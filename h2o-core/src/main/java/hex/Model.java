package hex;

import water.*;
import water.api.ModelSchema;
import water.fvec.*;
import water.util.ArrayUtils;
import water.util.ModelUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A Model models reality (hopefully).
 * A model can be used to 'score' a row (make a prediction), or a collection of
 * rows on any compatible dataset - meaning the row has all the columns with the
 * same names as used to build the mode and any enum (categorical) columns can
 * be adapted.
 */
public abstract class Model<M extends Model<M,P,O>, P extends Model.Parameters, O extends Model.Output> extends Lockable<M> {

  /** Different prediction categories for models.  NOTE: the values list in the API annotation ModelOutputSchema needs to match. */
  public static enum ModelCategory {
    Unknown,
    Binomial,
    Multinomial,
    Regression,
    Clustering
  }

  public boolean isSupervised() { return false; }

  /** Model-specific parameter class.  Each model sub-class contains an
   *  instance of one of these containing its builder parameters, with
   *  model-specific parameters.  E.g. KMeansModel extends Model and has a
   *  KMeansParameters extending Model.Parameters; sample parameters include K,
   *  whether or not to normalize, max iterations and the initial random seed.
   *
   *  <p>The non-transient fields are input parameters to the model-building
   *  process, and are considered "first class citizens" by the front-end - the
   *  front-end will cache Parameters (in the browser, in JavaScript, on disk)
   *  and rebuild Parameter instances from those caches.  
   */
  public abstract static class Parameters extends Iced {
    public Key _destination_key;     // desired Key for this model (otherwise is autogenerated)
    public Key _train;               // User-Key of the Frame the Model is trained on
    public Key _valid;               // User-Key of the Frame the Model is validated on, if any
    // TODO: This field belongs in the front-end column-selection process and
    // NOT in the parameters - because this requires all model-builders to have
    // column strip/ignore code.
    public String[] _ignored_columns;// column names to ignore for training
    public boolean _dropNA20Cols;    // True if dropping cols > 20% NAs

    // Scoring a model on a dataset is not free; sometimes it is THE limiting
    // factor to model building.  By default, partially built models are only
    // scored every so many major model iterations - throttled to limit scoring
    // costs to less than 10% of the build time.  This flag forces scoring for
    // every iteration, allowing e.g. more fine-grained progress reporting.
    public boolean _score_each_iteration;

    /** For classification models, the maximum size (in terms of classes) of
     *  the confusion matrix for it to be printed. This option is meant to
     *  avoid printing extremely large confusion matrices.  */
    public int _max_confusion_matrix_size = 20;

    // Public no-arg constructor for reflective creation
    public Parameters() { _dropNA20Cols = defaultDropNA20Cols(); }

    /** @return the training frame instance */
    public final Frame train() { return _train.get(); }
    /** @return the validation frame instance, or the training frame
     *  if a validation frame was not specified */
    public final Frame valid() { return _valid==null ? train() : _valid.<Frame>get(); }

    /** Read-Lock both training and validation User frames. */
    public void lock_frames( Job job ) { 
      train().read_lock(job._key);
      if( _valid != null && !_train.equals(_valid) )
        valid().read_lock(job._key);
    }

    /** Read-UnLock both training and validation User frames. */
    public void unlock_frames( Job job ) {
      Frame tr = train();
      if( tr != null ) tr.unlock(job._key);
      if( _valid != null && !_train.equals(_valid) )
        valid().unlock(job._key);
    }

    // Override in subclasses to change the default; e.g. true in GLM
    protected boolean defaultDropNA20Cols() { return false; }
    
    /** Type of missing columns during adaptation between train/test datasets
     *  Overload this method for models that have sparse data handling - a zero
     *  will preserve the sparseness.  Otherwise, NaN is used.
     *  @return real-valued number (can be NaN)  */
    protected double missingColumnsType() { return Double.NaN; }

    public long checksum() {
      return 
        (_dropNA20Cols?17:1) *
        train().checksum() *
        (_valid == null ? 17 : valid().checksum()) *
        (null == _ignored_columns ? 23: Arrays.hashCode(_ignored_columns));
    }
  }

  public P _parms; // TODO: move things around so that this can be protected

  public String [] _warnings = new String[0];

  public void addWarning(String s){
    _warnings = Arrays.copyOf(_warnings,_warnings.length+1);
    _warnings[_warnings.length-1] = s;
  }

  /** Model-specific output class.  Each model sub-class contains an instance
   *  of one of these containing its "output": the pieces of the model needed
   *  for scoring.  E.g. KMeansModel has a KMeansOutput extending Model.Output
   *  which contains the clusters.  The output also includes the names, domains
   *  and other fields which are determined at training time.  */
  public abstract static class Output extends Iced {
    /** Columns used in the model and are used to match up with scoring data
     *  columns.  The last name is the response column name (if any). */
    public String _names[];
    /** Returns number of input features (OK for most unsupervised methods, need to override for supervised!) */
    public int nfeatures() { return _names.length; }

    /** Categorical/factor/enum mappings, per column.  Null for non-enum cols.
     *  Columns match the post-init cleanup columns.  The last column holds the
     *  response col enums for SupervisedModels.  */
    public String _domains[][];

    /** List of all the associated ModelMetrics objects, so we can delete them
     *  when we delete this model. */
    public Key[] _model_metrics = new Key[0];

    /** Job state (CANCELLED, FAILED, DONE).  TODO: Really the whole Job
     *  (run-time, etc) but that has to wait until Job is split from
     *  ModelBuilder. */
    public Job.JobState _state;

    /** The start time in mS since the epoch for model training, again this
     *  comes from the Job which needs to split from ModelBuilder.  */
    public long _training_start_time = 0L;

    /** The duration in mS for model training, again this comes from the Job
     *  which needs to split from ModelBuilder.  */
    public long _training_duration_in_ms = 0L;

    /** Any final prep-work just before model-building starts, but after the
     *  user has clicked "go".  E.g., converting a response column to an enum
     *  touches the entire column (can be expensive), makes a parallel vec
     *  (Key/Data leak management issues), and might throw IAE if there are too
     *  many classes. */
    public Output( ModelBuilder b ) {
      if( b.error_count() > 0 )
        throw new IllegalArgumentException(b.validationErrors());
      // Capture the data "shape" the model is valid on
      _names  = b._train.names  ();
      _domains= b._train.domains();
    }

    /** The names of all the columns, including the response column (which comes last). */
    public String[] allNames() { return _names; }
    /** The name of the response column (which is always the last column). */
    public String responseName() { return   _names[  _names.length-1]; }
    /** The names of the levels for an enum (categorical) response column. */
    public String[] classNames() { return _domains[_domains.length-1]; }
    /** Is this model a classification model? (v. a regression or clustering model) */
    public boolean isClassifier() { return classNames() != null ; }
    public int nclasses() {
      String cns[] = classNames();
      return cns==null ? 1 : cns.length;
    }

    // Note: Clustering algorithms MUST redefine this method to return ModelCategory.Clustering:
    public ModelCategory getModelCategory() {
      return (isClassifier() ?
              (nclasses() > 2 ? ModelCategory.Multinomial : ModelCategory.Binomial) :
              ModelCategory.Regression);
    }

    // TODO: Needs to be Atomic update, not just synchronized
    protected synchronized ModelMetrics addModelMetrics(ModelMetrics mm) {
      for( int i=0; i<_model_metrics.length; i++ ) // Dup removal
        if( _model_metrics[i]==mm._key ) return mm;
      _model_metrics = Arrays.copyOf(_model_metrics, _model_metrics.length + 1);
      _model_metrics[_model_metrics.length - 1] = mm._key;
      return mm;                // Flow coding
    }

    public long checksum() {
      return (null == _names ? 13 : Arrays.hashCode(_names)) *
              (null == _domains ? 17 : Arrays.deepHashCode(_domains)) *
              getModelCategory().ordinal();
    }
  } // Output

  public O _output; // TODO: move things around so that this can be protected


  /**
   * Externally visible default schema
   * TODO: this is in the wrong layer: the internals should not know anything about the schemas!!!
   * This puts a reverse edge into the dependency graph.
   */
  public abstract ModelSchema schema();

  /** Full constructor */
  public Model( Key selfKey, P parms, O output) {
    super(selfKey);
    _parms  = parms ;  assert parms  != null;
    _output = output;  assert output != null;
  }


  /** Adapt a Test/Validation Frame to be compatible for a Training Frame.  The
   *  intention here is that ModelBuilders can assume the test set has the same
   *  count of columns, and within each factor column the same set of
   *  same-numbered levels.  Extra levels are renumbered past those in the
   *  Train set but will still be present in the Test set, thus requiring
   *  range-checking.
   *
   *  This routine is used before model building (with no Model made yet) to
   *  check for compatible datasets, and also used to prepare a large dataset
   *  for scoring (with a Model).
   * 
   *  Adaption does the following things:
   *  - Remove any "extra" Vecs appearing only in the test and not the train
   *  - Insert any "missing" Vecs appearing only in the train and not the test
   *    with all NAs ({@see missingColumnsType}).  This will issue a warning,
   *    and if the "expensive" flag is false won't actually make the column
   *    replacement column but instead will bail-out on the whole adaption (but
   *    will continue looking for more warnings).
   *  - If all columns are missing, issue an error.
   *  - Renumber matching cat levels to match the Train levels; this might make
   *    "holes" in the Test set cat levels, if some are not in the Test set.
   *  - Extra Test levels are renumbered past the end of the Train set, hence
   *    the train and test levels match up to all the train levels; there might
   *    be extra Test levels past that.
   *  - For all mis-matched levels, issue a warning.
   *
   *  The {@code test} frame is updated in-place to be compatible, by altering
   *  the names and Vecs; make a defensive copy if you do not want it modified.
   *  There is a fast-path cutout if the test set is already compatible.  Since
   *  the test-set is conditionally modifed with extra TransfVec optionally
   *  added it is recommended to use a Scope enter/exit to track Vec lifetimes.
   *
   *  @param test Testing Frame, updated in-place
   *  @param expensive Try hard to adapt; this might involve the creation of
   *  whole Vecs and thus get expensive.  If {@code false}, then only adapt if
   *  no warnings and errors; otherwise just the messages are produced.
   *  Created Vecs have to be deleted by the caller (e.g. Scope.enter/exit).
   *  @return Array of warnings; zero length (never null) for no warnings.
   *  Throws {@code IllegalArgumentException} if no columns are in common, or
   *  if any factor column has no levels in common.
   */
  public String[] adaptTestForTrain( Frame test, boolean expensive ) { return adaptTestForTrain( _output._names, _output._domains, test, _parms.missingColumnsType(), expensive); }
  /**
   *  @param names Training column names
   *  @param domains Training column levels
   *  @param missing Substitute for missing columns; usually NaN
   * */
  public static String[] adaptTestForTrain( String[] names, String[][] domains, Frame test, double missing, boolean expensive ) throws IllegalArgumentException {
    // Fast path cutout: already compatible
    String[][] tdomains = test.domains();
    if( names == test._names && domains == tdomains )
      return new String[0];
    // Fast path cutout: already compatible but needs work to test
    if( Arrays.equals(names,test._names) && Arrays.deepEquals(domains,tdomains) )
      return new String[0];

    // Build the validation set to be compatible with the training set.
    // Toss out extra columns, complain about missing ones, remap enums
    ArrayList<String> msgs = new ArrayList<>();
    Vec vvecs[] = new Vec[names.length];
    int good = 0;               // Any matching column names, at all?
    for( int i=0; i<names.length; i++ ) {
      Vec vec = test.vec(names[i]); // Search in the given validation set
      // If the training set is missing in the validation set, complain and
      // fill in with NAs.
      if( vec == null ) {
        msgs.add("Validation set is missing training column "+names[i]);
        if( expensive ) {
          vec = test.anyVec().makeCon(missing);
          vec.setDomain(domains[i]);
        }
      } 
      if( vec != null ) {       // I have a column with a matching name
        if( domains[i] != null ) { // Result needs to be an enum
          TransfVec tvec = vec.adaptTo(domains[i]); // Convert to enum or throw IAE
          String[] ds = tvec.domain();
          assert ds!=null && ds.length >= domains[i].length;
          if( ds.length > domains[i].length )
            msgs.add("Validation column "+names[i]+" has levels not trained on: "+Arrays.toString(Arrays.copyOfRange(ds,domains[i].length,ds.length)));
          if( expensive ) { vec = tvec; good++; } // Keep it
          else { tvec.remove(); vec = null; } // No leaking if not-expensive
        } else if( vec.isEnum() ) {
          throw new IllegalArgumentException("Validation set has categorical column "+names[i]+" which is real-valued in the training data");
        } else {
          good++;      // Assumed compatible; not checking e.g. Strings vs UUID
        }
      }
      vvecs[i] = vec;
    }
    if( good == 0 )
      throw new IllegalArgumentException("Validation set has no columns in common with the training set");
    if( good == names.length )  // Only update if got something for all columns
      test.restructure(names,vvecs);
    return msgs.toArray(new String[msgs.size()]);
  }

  /** Bulk score the frame {@code fr}, producing a Frame result; the 1st
   *  Vec is the predicted class, the remaining Vecs are the probability
   *  distributions.  For Regression (single-class) models, the 1st and only
   *  Vec is the prediction value.  The result is in the DKV; caller is
   *  responsible for deleting.
   *
   * @param fr frame which should be scored
   * @return A new frame containing a predicted values. For classification it
   *         contains a column with prediction and distribution for all
   *         response classes. For regression it contains only one column with
   *         predicted values.
   */
  public final Frame score(Frame fr) throws IllegalArgumentException {
    long start_time = System.currentTimeMillis();
    Frame adaptFr = new Frame(fr);
    Vec sresp = _output.isClassifier() ? fr.vec(_output.responseName()) : null;
    adaptTestForTrain(adaptFr,true);   // Adapt
    Frame output = scoreImpl(fr,adaptFr); // Score

    // Log modest confusion matrices
    Vec mresp = output.vecs()[0]; // Modeled/predicted response
    String mdomain[] = mresp.domain(); // Domain of predictions (union of test and train)
    ConfusionMatrix2 cm = ModelMetrics.getFromDKV(this,fr)._cm;
    if( cm._arr.length < _parms._max_confusion_matrix_size/*Print size limitation*/ )
      water.util.Log.info(water.util.PrettyPrint.printConfusionMatrix(new StringBuilder(),cm._arr,mdomain,false));

    // Output is in the model's domain, but needs to be mapped to the scored
    // dataset's domain.  
    if( _output.isClassifier() ) {
      String sdomain[] = sresp.domain(); // Scored/test domain; can be null
      if( mdomain != sdomain && !Arrays.equals(mdomain,sdomain) )
        output.replace(0,new TransfVec(sresp.group().addVec(),sresp.get_espc(),sdomain,mresp._key));
    }
    
    // Remove temp keys.  TODO: Really should use Scope but Scope does not
    // currently allow nested-key-keepers.
    Vec[] vecs = adaptFr.vecs();
    for( int i=0; i<vecs.length; i++ )
      if( fr.find(vecs[i]) != -1 ) // Exists in the original frame?
        vecs[i] = null;            // Do not delete it
    adaptFr.delete();
    return output;
  }

  /** Score an already adapted frame.  Returns a new Frame with new result
   *  vectors, all in the DKV.  Caller responsible for deleting.  Input is
   *  already adapted to the Model's domain, so the output is also.  Also
   *  computes the metrics for this frame.
   *
   * @param adaptFrm
   * @return A Frame containing the prediction column, and class distribution
   */
  protected Frame scoreImpl(Frame fr, Frame adaptFrm) {
    assert Arrays.equals(_output._names,adaptFrm._names); // Already adapted
    // Build up the names & domains.
    final int nc = _output.nclasses();
    final int ncols = nc==1?1:nc+1; // Regression has 1 predict col; classification also has class distribution
    String[] names = new String[ncols];
    String[][] domains = new String[ncols][];
    names[0] = "predict";
    for(int i = 1; i < names.length; ++i)
      names[i] = _output.classNames()[i-1];
    domains[0] = nc==1 ? null : adaptFrm.lastVec().domain();
    // Score the dataset, building the class distribution & predictions
    BigScore bs = new BigScore(domains[0],ncols).doAll(ncols,adaptFrm);
    bs._mb.makeModelMetrics(this,fr, this instanceof SupervisedModel ? adaptFrm.lastVec().sigma() : Double.NaN);
    Frame res = bs.outputFrame(Key.make(),names,domains);
    DKV.put(res);
    return res;
  }
  private class BigScore extends MRTask<BigScore> {
    final String[] _domain; // Prediction domain; union of test and train classes
    final int _ncols;  // Number of columns in prediction; nclasses+1 - can be less than the prediction domain
    ModelMetrics.MetricBuilder _mb;
    BigScore( String[] domain, int ncols ) { _domain = domain; _ncols = ncols; }
    @Override public void map( Chunk chks[], NewChunk cpreds[] ) {
      Chunk ys = chks[chks.length-1]; // Adapted actuals are last column
      double[] tmp = new double[_output.nfeatures()];
      _mb = new ModelMetrics.MetricBuilder(_domain,_output.nclasses()==2 ? ModelUtils.DEFAULT_THRESHOLDS : new float[]{0.5f});
      float[] preds = _mb._work;  // Sized for the union of test and train classes
      int len = chks[0]._len;
      for( int row=0; row<len; row++ ) {
        float[] p = score0(chks,row,tmp,preds);
        _mb.perRow(preds,(float)ys.at0(row));
        for( int c=0; c<_ncols; c++ )  // Output predictions; sized for train only (excludes extra test classes)
          cpreds[c].addNum(p[c]);
      }
    }
    @Override public void reduce( BigScore bs ) { _mb.reduce(bs._mb); }
  }

  /** Bulk scoring API for one row.  Chunks are all compatible with the model,
   *  and expect the last Chunks are for the final distribution and prediction.
   *  Default method is to just load the data into the tmp array, then call
   *  subclass scoring logic. */
  public float[] score0( Chunk chks[], int row_in_chunk, double[] tmp, float[] preds ) {
    assert chks.length>=_output._names.length;
    for( int i=0; i<_output._names.length; i++ )
      tmp[i] = chks[i].at0(row_in_chunk);
    return score0(tmp,preds);
  }

  /** Subclasses implement the scoring logic.  The data is pre-loaded into a
   *  re-used temp array, in the order the model expects.  The predictions are
   *  loaded into the re-used temp array, which is also returned.  */
  protected abstract float[] score0(double data[/*ncols*/], float preds[/*nclasses+1*/]);
  // Version where the user has just ponied-up an array of data to be scored.
  // Data must be in proper order.  Handy for JUnit tests.
  public double score(double [] data){ return ArrayUtils.maxIndex(score0(data, new float[_output.nclasses()]));  }

  @Override protected Futures remove_impl( Futures fs ) {
    for( Key k : _output._model_metrics )
      k.remove(fs);
    return fs;
  }

  @Override public long checksum() { return _parms.checksum() * _output.checksum(); }
}
