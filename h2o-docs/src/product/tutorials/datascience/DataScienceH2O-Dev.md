# Data Science Algorithms

This document describes how to define the models and how to interpret the model, as well the algorithm itself, and provides an FAQ. 

##Commonalities 

###Missing Value Handling for Training

If missing values are found in the validation frame during model training or during the scoring process for creating predictions, the missing values are automatically imputed. 

If the missing values are found during POJO scoring, the answer is converted to `NaN`. 


<a name="Kmeans"></a>
##K-Means

###Introduction

K-Means falls in the general category of clustering algorithms.

###Defining a K-Means Model

- **Model_id**: (Optional) Enter a custom name for the model to use as a reference. By default, H2O automatically generates a destination key. 

- **Training_frame**: (Required) Select the dataset used to build the model. 
**NOTE**: If you click the **Build a model** button from the `Parse` cell, the training frame is entered automatically. 

- **Validation_frame**: (Optional) Select the dataset used to evaluate the accuracy of the model. 

- **Ignored_columns**: (Optional) Click the checkbox next to a column name to add it to the list of columns excluded from the model. To add all columns, click the **Select All** button. To remove a column from the list of ignored columns, click the X next to the column name. To remove all columns from the list of ignored columns, click the **Deselect All** button. To search for a specific column, type the column name in the **Search** field above the column list. To only show columns with a specific percentage of missing values, specify the percentage in the **Only show columns with more than 0% missing values** field. To change the selections for the hidden columns, use the **Select Visible** or **Deselect Visible** buttons. 

- **Ignore\_const\_cols**: (Optional) Check this checkbox to ignore constant training columns, since no information can be gained from them. This option is selected by default. 

- **K**: Specify the number of clusters.  

- **User_points**: Specify a vector of initial cluster centers.

- **Max_iterations**: Specify the maximum number of training iterations. 

- **Init**: Select the initialization mode. The options are Random, Furthest, PlusPlus, or User. **Note**: If PlusPlus is selected, the initial Y matrix is chosen by the final cluster centers from the K-Means PlusPlus algorithm. 

- **Score\_each\_iteration**: (Optional) Check this checkbox to score during each iteration of the model training. 

- **Standardize**: To standardize the numeric columns to have mean of zero and unit variance, check this checkbox. Standardization is highly recommended; if you do not use standardization, the results can include components that are dominated by variables that appear to have larger variances relative to other attributes as a matter of scale, rather than true contribution. This option is selected by default. 

 >**Note**: If standardization is enabled, each column of numeric data is centered and scaled so that its mean is zero and its standard deviation is one before the algorithm is used. At the end of the process, the cluster centers on both the standardized scale (`centers_std`) and the de-standardized scale (`centers`) are displayed. 
 >To de-standardize the centers, the algorithm multiplies by the original standard deviation of the corresponding column and adds the original mean. Enabling standardization is mathematically equivalent to using `h2o.scale` in R with `center` = TRUE and `scale` = TRUE on the numeric columns. Therefore, there will be no discernible difference if standardization is enabled or not for K-Means, since H2O calculates unstandardized centroids. 
 
- **Seed**: Specify the random number generator (RNG) seed for algorithm components dependent on randomization. The seed is consistent for each H2O instance so that you can create models with the same starting conditions in alternative configurations. 

###Interpreting a K-Means Model

By default, the following output displays:

- A graph of the scoring history (number of iterations vs. average within the cluster's sum of squares) 
- Output (model category, validation metrics if applicable, and centers std)
- Model Summary (number of clusters, number of categorical columns, number of iterations, avg. within sum of squares, avg. sum of squares, avg. between the sum of squares)
- Scoring history (number of iterations, avg. change of standardized centroids, avg. within cluster sum of squares)
- Training metrics (model name, checksum name, frame name, frame checksum name, description if applicable, model category, duration in ms, scoring time, predictions, MSE, avg. within sum of squares, avg. between sum of squares)
- Centroid statistics (centroid number, size, within sum of squares)
- Cluster means (centroid number, column)

K-Means randomly chooses starting points and converges to a local minimum of centroids. The number of clusters is arbitrary, and should be thought of as a tuning parameter.
The output is a matrix of the cluster assignments and the coordinates of the cluster centers in terms of the originally chosen attributes. Your cluster centers may differ slightly from run to run as this problem is Non-deterministic Polynomial-time (NP)-hard.

###FAQ

- **How does the algorithm handle missing values during training?**
   
  Missing values are automatically imputed by the column mean.

- **How does the algorithm handle missing values during testing?**
   
  Missing values are automatically imputed by the column mean of the training data.

- **Does it matter if the data is sorted?** 
  
  No.

- **Should data be shuffled before training?**
  
  No.

- **What if there are a large number of columns?**
  
  K-Means suffers from the curse of dimensionality: all points are roughly at the same distance from each other in high dimensions, making the algorithm less and less useful.

- **What if there are a large number of categorical factor levels?**

  This can be problematic, as categoricals are one-hot encoded on the fly, which can lead to the same problem as datasets with a large number of columns.



###K-Means Algorithm

The number of clusters \(K\) is user-defined and is determined a priori. 

1. Choose \(K\) initial cluster centers \(m_{k}\) according to one of
   the following:

    - **Randomization**: Choose \(K\) clusters from the set of \(N\) observations at random so that each observation has an equal chance of being chosen.

    - **Plus Plus**  

      a. Choose one center \(m_{1}\) at random. 

      2.  Calculate the difference between \(m_{1}\) and each of the remaining \(N-1\) observations \(x_{i}\). 
  \(d(x_{i}, m_{1}) = ||(x_{i}-m_{1})||^2\)

      3. Let \(P(i)\) be the probability of choosing \(x_{i}\) as \(m_{2}\). Weight \(P(i)\) by \(d(x_{i}, m_{1})\) so that those \(x_{i}\) furthest from \(m_{2}\) have  a higher probability of being selected than those \(x_{i}\) close to \(m_{1}\).

      4. Choose the next center \(m_{2}\) by drawing at random according to the weighted probability distribution. 

      5.  Repeat until \(K\) centers have been chosen.

   - **Furthest**

       a. Choose one center \(m_{1}\) at random. 

       2. Calculate the difference between \(m_{1}\) and each of the remaining \(N-1\) observations \(x_{i}\). 
       \(d(x_{i}, m_{1}) = ||(x_{i}-m_{1})||^2\)

       3. Choose \(m_{2}\) to be the \(x_{i}\) that maximizes \(d(x_{i}, m_{1})\).

       4. Repeat until \(K\) centers have been chosen. 

2. Once \(K\) initial centers have been chosen calculate the difference between each observation \(x_{i}\) and each of the centers \(m_{1},...,m_{K}\), where difference is the squared Euclidean distance taken over \(p\) parameters.  
  
   \(d(x_{i}, m_{k})=\)
   \(\sum_{j=1}^{p}(x_{ij}-m_{k})^2=\)
 \(\lVert(x_{i}-m_{k})\rVert^2\)


3. Assign \(x_{i}\) to the cluster \(k\) defined by \(m_{k}\) that minimizes \(d(x_{i}, m_{k})\)

4. When all observations \(x_{i}\) are assigned to a cluster calculate the mean of the points in the cluster. 

	\(\bar{x}(k)=\lbrace\bar{x_{i1}},…\bar{x_{ip}}\rbrace\)

5. Set the \(\bar{x}(k)\) as the new cluster centers \(m_{k}\). Repeat steps 2 through 5 until the specified number of max iterations is reached or cluster assignments of the \(x_{i}\) are stable.



###References

[Hastie, Trevor, Robert Tibshirani, and J Jerome H Friedman. The Elements of Statistical Learning. Vol.1. N.p., Springer New York, 2001.](http://www.stanford.edu/~hastie/local.ftp/Springer/OLD//ESLII_print4.pdf)

Xiong, Hui, Junjie Wu, and Jian Chen. “K-means Clustering Versus Validation Measures: A Data- distribution Perspective.” Systems, Man, and Cybernetics, Part B: Cybernetics, IEEE Transactions on 39.2 (2009): 318-331.

---

<a name="GLM"></a>
##GLM

###Introduction

Generalized Linear Models (GLM) estimate regression models for outcomes following exponential distributions. In addition to the Gaussian (i.e. normal) distribution, these include Poisson, binomial, and gamma distributions. Each serves a different purpose, and depending on distribution and link function choice, can be used either for prediction or classification.

The GLM suite includes:

- Gaussian regression
- Poisson regression
- Binomial regression
- Gamma regression


###Defining a GLM Model

- **Model_id**: (Optional) Enter a custom name for the model to use as a reference. By default, H2O automatically generates a destination key. 

- **Training_frame**: (Required) Select the dataset used to build the model. 
**NOTE**: If you click the **Build a model** button from the `Parse` cell, the training frame is entered automatically. 

- **Validation_frame**: (Optional) Select the dataset used to evaluate the accuracy of the model. 

- **Nfolds**: Specify the number of folds for cross-validation. 

- **Fold_column**: Select the column that contains the cross-validation fold index assignment per observation. 

- **Response_column**: (Required) Select the column to use as the independent variable.

- **Ignored_columns**: (Optional) Click the checkbox next to a column name to add it to the list of columns excluded from the model. To add all columns, click the **Select All** button. To remove a column from the list of ignored columns, click the X next to the column name. To remove all columns from the list of ignored columns, click the **Deselect All** button. To search for a specific column, type the column name in the **Search** field above the column list. To only show columns with a specific percentage of missing values, specify the percentage in the **Only show columns with more than 0% missing values** field. To change the selections for the hidden columns, use the **Select Visible** or **Deselect Visible** buttons. 

- **Ignore\_const\_cols**: Check this checkbox to ignore constant training columns, since no information can be gained from them. This option is selected by default. 

- **Offset_column**: Select a column to use as the offset. 
	>*Note*: Offsets are per-row "bias values" that are used during model training. For Gaussian distributions, they can be seen as simple corrections to the response (y) column. Instead of learning to predict the response (y-row), the model learns to predict the (row) offset of the response column. For other distributions, the offset corrections are applied in the linearized space before applying the inverse link function to get the actual response values. For more information, refer to the following [link](http://www.idg.pl/mirrors/CRAN/web/packages/gbm/vignettes/gbm.pdf). 

- **Weights_column**: Select a column to use for the observation weights, which are used for bias correction.
	>*Note*: Weights are per-row observation weights. This is typically the number of times a row is repeated, but non-integer values are supported as well. During training, rows with higher weights matter more, due to the larger loss function pre-factor.  

- **Family**: Select the model type (Gaussian, Binomial, Poisson, Gamma, or Tweedie).

- **Tweedie_variance_power**: (Only applicable if *Tweedie* is selected for **Family**) Specify the Tweedie variance power. 

- **Tweedie_link_power**: (Only applicable if *Tweedie* is selected for **Family**) Specify the Tweedie link power. 

- **Solver**: Select the solver to use (IRLSM, L\_BFGS, or auto). IRLSM is fast on problems with small number of predictors and for lambda-search with L1 penalty, while [L_BFGS](http://cran.r-project.org/web/packages/lbfgs/vignettes/Vignette.pdf) scales better for datasets with many columns.  

- **Alpha**: Specify the regularization distribution between L2 and L2.  

- **Lambda**:  Specify the regularization strength. 

- **Lambda_search**: Check this checkbox to enable lambda search, starting with lambda max. The given lambda is then interpreted as lambda min. 

- **Standardize**: To standardize the numeric columns to have a mean of zero and unit variance, check this checkbox. Standardization is highly recommended; if you do not use standardization, the results can include components that are dominated by variables that appear to have larger variances relative to other attributes as a matter of scale, rather than true contribution. This option is selected by default. 

- **Non-negative**: To force coefficients to have non-negative values, check this checkbox. 

- **Beta constraints**: To use beta constraints, select a dataset from the drop-down menu. The selected frame is used to constraint the coefficient vector to provide upper and lower bounds. 

- **Score\_each\_iteration**: (Optional) Check this checkbox to score during each iteration of the model training. 

- **Max_iterations**: Specify the number of training iterations.   

- **Link**: Select a link function (Identity, Family_Default, Logit, Log, Inverse, or Tweedie).

- **Max\_confusion\_matrix\_size**: Specify the maximum size (number of classes) for the confusion matrices printed in the logs. 

- **Max\_hit\_ratio\_k**: Specify the maximum number (top K) of predictions to use for hit ratio computation. Applicable to multi-class only. To disable, enter `0`. 

- **Keep\_cross\_validation\_splits**: To keep the cross-validation frames, check this checkbox. 

- **Fold_assignment**: (Applicable only if a value for **nfolds** is specified and **fold_column** is not selected) Select the cross-validation fold assignment scheme. The available options are Random or [Modulo](https://en.wikipedia.org/wiki/Modulo_operation). 

- **Intercept**: To include a constant term in the model, check this checkbox. This option is selected by default. 

- **Objective_epsilon**: Specify a threshold for convergence. If the objective value is less than this threshold, the model is converged. 

- **Beta_epsilon**: Specify the beta epsilon value. If the L1 normalization of the current beta change is below this threshold, consider using convergence. 

- **Gradient_epsilon**: (For L-BFGS only) Specify a threshold for convergence. If the objective value (using the L-infinity norm) is less than this threshold, the model is converged. 

- **Prior**: Specify prior probability for y ==1. Use this parameter for logistic regression if the data has been sampled and the mean of response does not reflect reality.  

- **Max\_active\_predictors**: Specify the maximum number of active predictors during computation. This value is used as a stopping criterium to prevent expensive model building with many predictors. 


###Interpreting a GLM Model

By default, the following output displays:

- A graph of the normalized coefficient magnitudes
- Output (model category, model summary, scoring history, training metrics, validation metrics, best lambda, threshold, residual deviance, null deviance, residual degrees of freedom, null degrees of freedom, AIC, AUC, binomial, rank)
- Coefficients
- Coefficient magnitudes

###FAQ

- **How does the algorithm handle missing values during training?**

  GLM skips rows with missing values.

- **How does the algorithm handle missing values during testing?**

  GLM will predict Double.NaN for rows containg missing values.

- **What happens if the response has missing values?**

  It is handled properly, but verify the results are correct.

- **What happens during prediction if the new sample has categorical levels not seen in training?**

  It will predict Double.NaN.

- **Does it matter if the data is sorted?** 

  No.

- **Should data be shuffled before training?**

  No.

- **How does the algorithm handle highly imbalanced data in a response column?**

  GLM does not require special handling for imbalanced data.

- **What if there are a large number of columns?**

  IRLS will get quadratically slower with the number of columns. Try L-BFGS for datasets with more than 5-10 thousand columns.

- **What if there are a large number of categorical factor levels?**

  GLM internally one-hot encodes the categorical factor levels; the same limitations as with a high column count will apply.

###GLM Algorithm

Following the definitive text by P. McCullagh and J.A. Nelder (1989) on the generalization of linear models to non-linear distributions of the response variable Y, H2O fits GLM models based on the maximum likelihood estimation via iteratively reweighed least squares. 

Let \(y_{1},…,y_{n}\) be n observations of the independent, random response variable \(Y_{i}\).

Assume that the observations are distributed according to a function from the exponential family and have a probability density function of the form:

\(f(y_{i})=exp[\frac{y_{i}\theta_{i} - b(\theta_{i})}{a_{i}(\phi)} + c(y_{i}; \phi)]\)
where \(\theta\) and \(\phi\) are location and scale parameters,
and \(\: a_{i}(\phi), \:b_{i}(\theta_{i}),\: c_{i}(y_{i}; \phi)\) are known functions.

\(a_{i}\) is of the form \(\:a_{i}=\frac{\phi}{p_{i}}; p_{i}\) is a known prior weight.

When \(Y\) has a pdf from the exponential family: 

\(E(Y_{i})=\mu_{i}=b^{\prime}\)
\(var(Y_{i})=\sigma_{i}^2=b^{\prime\prime}(\theta_{i})a_{i}(\phi)\)

Let \(g(\mu_{i})=\eta_{i}\) be a monotonic, differentiable transformation of the expected value of \(y_{i}\). The function \(\eta_{i}\) is the link function and follows a linear model.

\(g(\mu_{i})=\eta_{i}=\mathbf{x_{i}^{\prime}}\beta\)

When inverted: 
\(\mu=g^{-1}(\mathbf{x_{i}^{\prime}}\beta)\)

**Maximum Likelihood Estimation**

For an initial rough estimate of the parameters \(\hat{\beta}\), use the estimate to generate fitted values: 
\(\mu_{i}=g^{-1}(\hat{\eta_{i}})\)

Let \(z\) be a working dependent variable such that 
\(z_{i}=\hat{\eta_{i}}+(y_{i}-\hat{\mu_{i}})\frac{d\eta_{i}}{d\mu_{i}}\),

where \(\frac{d\eta_{i}}{d\mu_{i}}\) is the derivative of the link function evaluated at the trial estimate. 

Calculate the iterative weights:
\(w_{i}=\frac{p_{i}}{[b^{\prime\prime}(\theta_{i})\frac{d\eta_{i}}{d\mu_{i}}^{2}]}\)

Where \(b^{\prime\prime}\) is the second derivative of \(b(\theta_{i})\) evaluated at the trial estimate. 


Assume \(a_{i}(\phi)\) is of the form \(\frac{\phi}{p_{i}}\). The weight \(w_{i}\) is inversely proportional to the variance of the working dependent variable \(z_{i}\) for current parameter estimates and proportionality factor \(\phi\).

Regress \(z_{i}\) on the predictors \(x_{i}\) using the weights \(w_{i}\) to obtain new estimates of \(\beta\). 
\(\hat{\beta}=(\mathbf{X}^{\prime}\mathbf{W}\mathbf{X})^{-1}\mathbf{X}^{\prime}\mathbf{W}\mathbf{z}\) 

Where \(\mathbf{X}\) is the model matrix, \(\mathbf{W}\) is a diagonal matrix of \(w_{i}\), and \(\mathbf{z}\) is a vector of the working response variable \(z_{i}\).

This process is repeated until the estimates \(\hat{\beta}\) change by less than the specified amount. 

**Cost of computation**


H2O can process large data sets because it relies on parallel processes. Large data sets are divided into smaller data sets and processed simultaneously and the results are communicated between computers as needed throughout the process. 

In GLM, data are split by rows but not by columns, because the predicted Y values depend on information in each of the predictor variable vectors. If O is a complexity function, N is the number of observations (or rows), and P is the number of predictors (or columns) then 


   &nbsp;&nbsp;&nbsp;&nbsp;\(Runtime\propto p^3+\frac{(N*p^2)}{CPUs}\)

Distribution reduces the time it takes an algorithm to process because it decreases N.
 

Relative to P, the larger that (N/CPUs) becomes, the more trivial p becomes to the overall computational cost. However, when p is greater than (N/CPUs), O is dominated by p.



   &nbsp;&nbsp;&nbsp;&nbsp;\(Complexity = O(p^3 + N*p^2)\) 




###References

Breslow, N E. “Generalized Linear Models: Checking Assumptions and Strengthening Conclusions.” Statistica Applicata 8 (1996): 23-41.

[Frome, E L. “The Analysis of Rates Using Poisson Regression Models.” Biometrics (1983): 665-674.](http://www.csm.ornl.gov/~frome/BE/FP/FromeBiometrics83.pdf)

[Goldberger, Arthur S. “Best Linear Unbiased Prediction in the Generalized Linear Regression Model.” Journal of the American Statistical Association 57.298 (1962): 369-375.](http://people.umass.edu/~bioep740/yr2009/topics/goldberger-jasa1962-369.pdf)

[Guisan, Antoine, Thomas C Edwards Jr, and Trevor Hastie. “Generalized Linear and Generalized Additive Models in Studies of Species Distributions: Setting the Scene.” Ecological modeling 157.2 (2002): 89-100.](http://www.stanford.edu/~hastie/Papers/GuisanEtAl_EcolModel-2003.pdf)

[Nelder, John A, and Robert WM Wedderburn. “Generalized Linear Models.” Journal of the Royal Statistical Society. Series A (General) (1972): 370-384.](http://biecek.pl/MIMUW/uploads/Nelder_GLM.pdf)

[Niu, Feng, et al. “Hogwild!: A lock-free approach to parallelizing stochastic gradient descent.” Advances in Neural Information Processing Systems 24 (2011): 693-701.*implemented algorithm on p.5](http://www.eecs.berkeley.edu/~brecht/papers/hogwildTR.pdf)

[Pearce, Jennie, and Simon Ferrier. “Evaluating the Predictive Performance of Habitat Models Developed Using Logistic Regression.” Ecological modeling 133.3 (2000): 225-245.](http://www.whoi.edu/cms/files/Ecological_Modelling_2000_Pearce_53557.pdf)

[Press, S James, and Sandra Wilson. “Choosing Between Logistic Regression and Discriminant Analysis.” Journal of the American Statistical Association 73.364 (April, 2012): 699–705.](http://www.statpt.com/logistic/press_1978.pdf)

Snee, Ronald D. “Validation of Regression Models: Methods and Examples.” Technometrics 19.4 (1977): 415-428.

---


<a name="DRF"></a>
##DRF

###Introduction

Distributed Random Forest (DRF) is a powerful classification tool. When given a set of data, DRF generates a forest of classification trees, rather than a single classification tree. Each of these trees is a weak learner built on a subset of rows and columns. More trees will reduce the variance. The classification from each H2O tree can be thought of as a vote; the most votes determines the classification.

###Defining a DRF Model

- **Model_id**: (Optional) Enter a custom name for the model to use as a reference. By default, H2O automatically generates a destination key. 

- **Training_frame**: (Required) Select the dataset used to build the model. 
**NOTE**: If you click the **Build a model** button from the `Parse` cell, the training frame is entered automatically. 

- **Validation_frame**: (Optional) Select the dataset used to evaluate the accuracy of the model. 

- **Nfolds**: Specify the number of folds for cross-validation. 

- **Fold_column**: Select the column that contains the cross-validation fold index assignment per observation. 

- **Response_column**: (Required) Select the column to use as the independent variable.

- **Ignored_columns**: (Optional) Click the checkbox next to a column name to add it to the list of columns excluded from the model. To add all columns, click the **Select All** button. To remove a column from the list of ignored columns, click the X next to the column name. To remove all columns from the list of ignored columns, click the **Deselect All** button. To search for a specific column, type the column name in the **Search** field above the column list. To only show columns with a specific percentage of missing values, specify the percentage in the **Only show columns with more than 0% missing values** field. To change the selections for the hidden columns, use the **Select Visible** or **Deselect Visible** buttons. 

- **Ignore\_const\_cols**: Check this checkbox to ignore constant training columns, since no information can be gained from them. This option is selected by default. 

- **Offset_column**: Select a column to use as the offset. 
	>*Note*: Offsets are per-row "bias values" that are used during model training. For Gaussian distributions, they can be seen as simple corrections to the response (y) column. Instead of learning to predict the response (y-row), the model learns to predict the (row) offset of the response column. For other distributions, the offset corrections are applied in the linearized space before applying the inverse link function to get the actual response values. For more information, refer to the following [link](http://www.idg.pl/mirrors/CRAN/web/packages/gbm/vignettes/gbm.pdf). 

- **Weights_column**: Select a column to use for the observation weights, which are used for bias correction.
	>*Note*: Weights are per-row observation weights. This is typically the number of times a row is repeated, but non-integer values are supported as well. During training, rows with higher weights matter more, due to the larger loss function pre-factor.  

- **Ntrees**: Specify the number of trees.  

- **Max\_depth**: Specify the maximum tree depth.

- **Min\_rows**: Specify the minimum number of observations for a leaf (`nodesize` in R).  

- **Nbins**: (Numerical/real/int only) Specify the number of bins for the histogram to build, then split at the best point.  

- **Nbins_cats**: (Categorical/enums only) Specify the number of bins for the histogram to build, then split at the best point. Higher values can lead to more overfitting.  

- **Seed**: Specify the random number generator (RNG) seed for algorithm components dependent on randomization. The seed is consistent for each H2O instance so that you can create models with the same starting conditions in alternative configurations. 

- **Mtries**: Specify the columns to randomly select at each level. If the default value of `-1` is used, the number of variables is the square root of the number of columns for classification and p/3 for regression (where p is the number of predictors).   

- **Sample\_rate**: Specify the sample rate. The range is 0 to 1.0. 

- **Score\_each\_iteration**: (Optional) Check this checkbox to score during each iteration of the model training. 

- **Balance_classes**: Oversample the minority classes to balance the class distribution. This option is not selected by default. This option is only applicable for classification. 

- **Max\_confusion\_matrix\_size**: Specify the maximum size (in number of classes) for confusion matrices to be printed in the Logs. 

- **Max\_hit\_ratio\_k**: Specify the maximum number (top K) of predictions to use for hit ratio computation. Applicable to multi-class only. To disable, enter 0. 

- **R2_stopping**: Specify a threshold for the coefficient of determination (\(r^2\)) metric value. When this threshold is met or exceeded, H2O stops making trees. 

- **Build\_tree\_one\_node**: To run on a single node, check this checkbox. This is suitable for small datasets as there is no network overhead but fewer CPUs are used. 

- **Binomial\_double\_trees**: (Binary classification only) Build twice as many trees (one per class). Enabling this option can lead to higher accuracy, while disabling can result in faster model building. This option is disabled by default. 

- **Keep\_cross\_validation\_splits**: To keep the cross-validation frames, check this checkbox. 

- **Fold_assignment**: (Applicable only if a value for **nfolds** is specified and **fold_column** is not selected) Select the cross-validation fold assignment scheme. The available options are Random or Modulo. 

- **Class\_sampling\_factors**: Specify the per-class (in lexicographical order) over/under-sampling ratios. By default, these ratios are automatically computed during training to obtain the class balance.  

- **Max\_after\_balance\_size**: Specify the maximum relative size of the training data after balancing class counts (**balance\_classes** must be enabled). The value can be less than 1.0. 


###Interpreting a DRF Model

By default, the following output displays:

- Model parameters (hidden)  
- A graph of the scoring history (number of trees vs. training MSE)
- A graph of the ROC curve (TPR vs. FPR)
- A graph of the variable importances
- Output (model category, validation metrics, initf)
- Model summary (number of trees, min. depth, max. depth, mean depth, min. leaves, max. leaves, mean leaves)
- Scoring history in tabular format
- Training metrics (model name, checksum name, frame name, frame checksum name, description, model category, duration in ms, scoring time, predictions, MSE, R2, logloss, AUC, GINI)
- Training metrics for thresholds (thresholds, F1, F2, F0Points, Accuracy, Precision, Recall, Specificity, Absolute MCC, min. per-class accuracy, TNS, FNS, FPS, TPS, IDX)
- Maximum metrics (metric, threshold, value, IDX)
- Variable importances in tabular format

###FAQ

- **How does the algorithm handle missing values during training?**

  Missing values do not alter the tree building in any way (i.e., they are not counted as a point when computing means or errors). Rows containing missing values do affect tree building, but the missing values don't change the split-point of the column they are in.

- **How does the algorithm handle missing values during testing?**

  During scoring, missing values "always go left" at any decision point in a tree. Due to dynamic binning in DRF, a row with a missing value typically ends up in the "leftmost bin" - with other outliers.

- **What happens if the response has missing values?**
 
  No errors will occur, but nothing will be learned from rows containing missing the response.

- **Does it matter if the data is sorted?** 

  No.

- **Should data be shuffled before training?**
  
  No.

- **How does the algorithm handle highly imbalanced data in a response column?**

 Specify `balance_classes`, `class_sampling_factors` and `max_after_balance_size` to control over/under-sampling.

- **What if there are a large number of columns?**

  DRFs are best for datasets with fewer than a few thousand columns.

- **What if there are a large number of categorical factor levels?**

  Large numbers of categoricals are handled very efficiently - there is never any one-hot encoding.

###DRF Algorithm 


<iframe src="//www.slideshare.net/slideshow/embed_code/key/tASzUyJ19dtJsQ" width="425" height="355" frameborder="0" marginwidth="0" marginheight="0" scrolling="no" style="border:1px solid #CCC; border-width:1px; margin-bottom:5px; max-width: 100%;" allowfullscreen> </iframe> <div style="margin-bottom:5px"> <strong> <a href="//www.slideshare.net/0xdata/rf-brighttalk" title="Building Random Forest at Scale" target="_blank">Building Random Forest at Scale</a> </strong> from <strong><a href="//www.slideshare.net/0xdata" target="_blank">Sri Ambati</a></strong> </div>

###References

---

<a name="NB"></a>
##Naïve Bayes

###Introduction 

Naïve Bayes (NB) is a classification algorithm that relies on strong assumptions of the independence of covariates in applying Bayes Theorem. NB models are commonly used as an alternative to decision trees for classification problems.

###Defining a Naïve Bayes Model

- **Model_id**: (Optional) Enter a custom name for the model to use as a reference. By default, H2O automatically generates a destination key. 

- **Training_frame**: (Required) Select the dataset used to build the model. 
**NOTE**: If you click the **Build a model** button from the `Parse` cell, the training frame is entered automatically. 

- **Validation_frame**: (Optional) Select the dataset used to evaluate the accuracy of the model. 

- **Response_column**: (Required) Select the column to use as the independent variable.

- **Ignored_columns**: (Optional) Click the checkbox next to a column name to add it to the list of columns excluded from the model. To add all columns, click the **Select All** button. To remove a column from the list of ignored columns, click the X next to the column name. To remove all columns from the list of ignored columns, click the **Deselect All** button. To search for a specific column, type the column name in the **Search** field above the column list. To only show columns with a specific percentage of missing values, specify the percentage in the **Only show columns with more than 0% missing values** field. To change the selections for the hidden columns, use the **Select Visible** or **Deselect Visible** buttons. 

- **Ignore\_const\_cols**: Check this checkbox to ignore constant training columns, since no information can be gained from them. This option is selected by default. 

- **Laplace**: Specify the Laplace smoothing parameter.  

- **Min\_sdev**: Specify the minimum standard deviation to use for observations without enough data.   

- **Eps\_sdev**: Specify the threshold for standard deviation. If this threshold is not met, the **min\_sdev** value is used.  

- **Min\_prob**: Specify the minimum probability to use for observations without enough data.   

- **Eps\_prob**: Specify the threshold for standard deviation. If this threshold is not met, the **min\_sdev** value is used.  

- **Compute_metrics**: To compute metrics on training data, check this checkbox. The Naïve Bayes classifier assumes independence between predictor variables conditional on the response, and a Gaussian distribution of numeric predictors with mean and standard deviation computed from the training dataset. When building a Naïve Bayes classifier, every row in the training dataset that contains at least one NA will be skipped completely. If the test dataset has missing values, then those predictors are omitted in the probability calculation during prediction.

- **Score\_each\_iteration**: (Optional) Check this checkbox to score during each iteration of the model training. 

- **Max\_confusion\_matrix\_size**: Specify the maximum size (in number of classes) for confusion matrices to be printed in the Logs. 

- **Max\_hit\_ratio\_k**: Specify the maximum number (top K) of predictions to use for hit ratio computation. Applicable to multi-class only. To disable, enter 0. 



###Interpreting a Naïve Bayes Model

The output from Naïve Bayes is a list of tables containing the a-priori and conditional probabilities of each class of the response. The a-priori probability is the estimated probability of a particular class before observing any of the predictors. Each conditional probability table corresponds to a predictor column. The row headers are the classes of the response and the column headers are the classes of the predictor. Thus, in the table below, the probability of survival (y) given a person is male (x) is 0.91543624.

```
        		Sex
Survived       Male     Female
     No  0.91543624 0.08456376
     Yes 0.51617440 0.48382560
```


When the predictor is numeric, Naïve Bayes assumes it is sampled from a Gaussian distribution given the class of the response. The first column contains the mean and the second column contains the standard deviation of the distribution.

By default, the following output displays:

- Output (model category, model summary, scoring history, training metrics, validation metrics)
- Y-Levels (levels of the response column)
- P-conditionals 

###FAQ

- **How does the algorithm handle missing values during training?**
  
  All rows with one or more missing values (either in the predictors or the response) will be skipped during model building. 

- **How does the algorithm handle missing values during testing?**
  
  If a predictor is missing, it will be skipped when taking the product of conditional probabilities in calculating the joint probability conditional on the response.

- **What happens if the response domain is different in the training and test datasets?**
  
  The response column in the test dataset is not used during scoring, so any response categories absent in the training data will not be predicted.

- **What happens during prediction if the new sample has categorical levels not seen in training?**
  
  The conditional probability of that predictor level will be set according to the Laplace smoothing factor. If Laplace smoothing is disabled (set to zero), the joint probability will be zero. See pgs. 13-14 of Andrew Ng’s "Generative learning algorithms" in the References section for mathematical details.

- **Does it matter if the data is sorted?**

  No. 

- **Should data be shuffled before training?**

  This does not affect model building. 

- **How does the algorithm handle highly imbalanced data in a response column?**

  Unbalanced data will not affect the model. However, if one response category has very few observations compared to the total, the conditional probability may be very low. A cutoff (`eps_prob`) and minimum value (`min_prob`) are available for the user to set a floor on the calculated probability.


- **What if there are a large number of columns?**

   More memory will be allocated on each node to store the joint frequency counts and sums.

- **What if there are a large number of categorical factor levels?**

  More memory will be allocated on each node to store the joint frequency count of each categorical predictor level with the response’s level.



###Naïve Bayes Algorithm 

The algorithm is presented for the simplified binomial case without loss of generality.

Under the Naive Bayes assumption of independence, given a training set
for a set of discrete valued features X 
\({(X^{(i)},\ y^{(i)};\ i=1,...m)}\)

The joint likelihood of the data can be expressed as: 

\(\mathcal{L} \: (\phi(y),\: \phi_{i|y=1},\:\phi_{i|y=0})=\Pi_{i=1}^{m} p(X^{(i)},\: y^{(i)})\)

The model can be parameterized by:

\(\phi_{i|y=0}=\ p(x_{i}=1|\ y=0);\: \phi_{i|y=1}=\ p(x_{i}=1|y=1);\: \phi(y)\)

Where \(\phi_{i|y=0}=\ p(x_{i}=1|\ y=0)\) can be thought of as the fraction of the observed instances where feature \(x_{i}\) is observed, and the outcome is \(y=0, \phi_{i|y=1}=p(x_{i}=1|\ y=1)\) is the fraction of the observed instances where feature \(x_{i}\) is observed, and the outcome is \(y=1\), and so on.

The objective of the algorithm is to maximize with respect to
\(\phi_{i|y=0}, \ \phi_{i|y=1},\ and \ \phi(y)\)

Where the maximum likelihood estimates are: 

\(\phi_{j|y=1}= \frac{\Sigma_{i}^m 1(x_{j}^{(i)}=1 \ \bigcap y^{i} = 1)}{\Sigma_{i=1}^{m}(y^{(i)}=1}\)

\(\phi_{j|y=0}= \frac{\Sigma_{i}^m 1(x_{j}^{(i)}=1 \ \bigcap y^{i} = 0)}{\Sigma_{i=1}^{m}(y^{(i)}=0}\)

\(\phi(y)= \frac{(y^{i} = 1)}{m}\)


Once all parameters \(\phi_{j|y}\) are fitted, the model can be used to predict new examples with features \(X_{(i^*)}\). 

This is carried out by calculating: 

\(p(y=1|x)=\frac{\Pi p(x_i|y=1) p(y=1)}{\Pi p(x_i|y=1)p(y=1) \: +\: \Pi p(x_i|y=0)p(y=0)}\)

\(p(y=0|x)=\frac{\Pi p(x_i|y=0) p(y=0)}{\Pi p(x_i|y=1)p(y=1) \: +\: \Pi p(x_i|y=0)p(y=0)}\)

and predicting the class with the highest probability. 


It is possible that prediction sets contain features not originally seen in the training set. If this occurs, the maximum likelihood estimates for these features predict a probability of 0 for all cases of y. 

Laplace smoothing allows a model to predict on out of training data features by adjusting the maximum likelihood estimates to be: 


\(\phi_{j|y=1}= \frac{\Sigma_{i}^m 1(x_{j}^{(i)}=1 \ \bigcap y^{i} = 1) \: + \: 1}{\Sigma_{i=1}^{m}(y^{(i)}=1 \: + \: 2}\)

\(\phi_{j|y=0}= \frac{\Sigma_{i}^m 1(x_{j}^{(i)}=1 \ \bigcap y^{i} = 0) \: + \: 1}{\Sigma_{i=1}^{m}(y^{(i)}=0 \: + \: 2}\)

Note that in the general case where y takes on k values, there are k+1 modified parameter estimates, and they are added in when the denominator is k (rather than two, as shown in the two-level classifier shown here.)

Laplace smoothing should be used with care; it is generally intended to allow for predictions in rare events. As prediction data becomes increasingly distinct from training data, train new models when possible to account for a broader set of possible X values. 


###References


[Hastie, Trevor, Robert Tibshirani, and J Jerome H Friedman. The Elements of Statistical Learning. Vol.1. N.p., Springer New York, 2001.](http://www.stanford.edu/~hastie/local.ftp/Springer/OLD//ESLII_print4.pdf) 

[Ng, Andrew. "Generative Learning algorithms." (2008).](http://cs229.stanford.edu/notes/cs229-notes2.pdf)

---

<a name="PCA"></a>
##PCA

###Introduction

Principal Components Analysis (PCA) is closely related to Principal Components Regression. The algorithm is carried out on a set of possibly collinear features and performs a transformation to produce a new set of uncorrelated features.

PCA is commonly used to model without regularization or perform dimensionality reduction. It can also be useful to carry out as a preprocessing step before distance-based algorithms such as K-Means since PCA guarantees that all dimensions of a manifold are orthogonal.

###Defining a PCA Model

- **Model_id**: (Optional) Enter a custom name for the model to use as a reference. By default, H2O automatically generates a destination key. 

- **Training_frame**: (Required) Select the dataset used to build the model. 
**NOTE**: If you click the **Build a model** button from the `Parse` cell, the training frame is entered automatically. 

- **Validation_frame**: (Optional) Select the dataset used to evaluate the accuracy of the model. 

- **Ignored_columns**: (Optional) Click the checkbox next to a column name to add it to the list of columns excluded from the model. To add all columns, click the **Select All** button. To remove a column from the list of ignored columns, click the X next to the column name. To remove all columns from the list of ignored columns, click the **Deselect All** button. To search for a specific column, type the column name in the **Search** field above the column list. To only show columns with a specific percentage of missing values, specify the percentage in the **Only show columns with more than 0% missing values** field. To change the selections for the hidden columns, use the **Select Visible** or **Deselect Visible** buttons.

- **Ignore\_const\_cols**: Check this checkbox to ignore constant training columns, since no information can be gained from them. This option is selected by default.   

- **PCA_method**: Select the algorithm to use for computing the principal components: 
	- *GramSVD*: Uses a distributed computation of the Gram matrix, followed by a local SVD using the JAMA package
	- *Power*: Computes the SVD using the power iteration method
	- *GLRM*: Fits a generalized low-rank model with L2 loss function and no regularization and solves for the SVD using local matrix algebra

- **Use\_all\_factor\_levels**: Check this checkbox to use all factor levels in the possible set of predictors; if you enable this option, sufficient regularization is required. By default, the first factor level is skipped. For PCA models, this option ignores the first factor level of each categorical column when expanding into indicator columns. 

- **Loading_name**: (Applicable only if *Power* is selected for **PCA_method**) Specify a name for the frame to save left singular vectors from SVD. 

- **Score\_each\_iteration**: (Optional) Check this checkbox to score during each iteration of the model training. 

- **Transform**: Select the transformation method for the training data: None, Standardize, Normalize, Demean, or Descale. The default is None. 

- **K**: Specify the rank of matrix approximation. The default is 1.  

- **Max_iterations**: Specify the number of training iterations. The default is 1000.

- **Seed**: Specify the random number generator (RNG) seed for algorithm components dependent on randomization. The seed is consistent for each H2O instance so that you can create models with the same starting conditions in alternative configurations. 


###Interpreting a PCA Model

PCA output returns a table displaying the number of components specified by the value for `k`.

Scree and cumulative variance plots for the components are returned as well. Users can access them by clicking on the black button labeled "Scree and Variance Plots" at the top left of the results page. A scree plot shows the variance of each component, while the cumulative variance plot shows the total variance accounted for by the set of components.

The output for PCA includes the following: 

- Model parameters (hidden)
- Output (model category, model summary, scoring history, training metrics, validation metrics, iterations)
- Archetypes
- Standard deviation
- Rotation 
- Importance of components (standard deviation, proportion of variance, cumulative proportion) 



###FAQ

- **How does the algorithm handle missing values during scoring?**

For the GramSVD and Power methods, all rows containing missing values are ignored during training. For the GLRM method, missing values are excluded from the sum over the loss function in the objective. For more information, refer to section 4 Generalized Loss Functions, equation (13), in ["Generalized Low Rank Models"](https://web.stanford.edu/~boyd/papers/pdf/glrm.pdf) by Boyd et al.

  
- **How does the algorithm handle missing values during testing?**

  During scoring, the test data is right-multiplied by the eigenvector matrix produced by PCA. Missing categorical values are skipped in the row product-sum. Missing numeric values propagate an entire row of NAs in the resulting projection matrix.


- **What happens during prediction if the new sample has categorical levels not seen in training?**

  Categorical levels in the test data not present in the training data are skipped in the row product-sum.


- **Does it matter if the data is sorted?**
  
  No, sorting data does not affect the model. 
  
- **Should data be shuffled before training?**

  No, shuffling data does not affect the model. 


- **What if there are a large number of columns?**

  Calculating the SVD will be slower, since computations on the Gram matrix are handled locally. 

- **What if there are a large number of categorical factor levels?**

  Each factor level (with the exception of the first, depending on whether **use\_all\_factor\_levels** is enabled) is assigned an indicator column. The indicator column is 1 if the observation corresponds to a particular factor; otherwise, it is 0. As a result, many factor levels result in a large Gram matrix and slower computation of the SVD. 

- **How are categorical columns handled during model building?**
  
  If the GramSVD or Power methods are used, the categorical columns are expanded into 0/1 indicator columns for each factor level. The algorithm is then performed on this expanded training frame. For GLRM, the multidimensional loss function for categorical columns is discussed in Section 6.1 of ["Generalized Low Rank Models"](https://web.stanford.edu/~boyd/papers/pdf/glrm.pdf) by Boyd et al. 


###PCA Algorithm

Let \(X\) be an \(M\times N\) matrix where
 
- Each row corresponds to the set of all measurements on a particular 
   attribute, and 

- Each column corresponds to a set of measurements from a given
   observation or trial

The covariance matrix \(C_{x}\) is

\(C_{x}=\frac{1}{n}XX^{T}\)

where \(n\) is the number of observations. 

\(C_{x}\) is a square, symmetric \(m\times m\) matrix, the diagonal entries of which are the variances of attributes, and the off-diagonal entries are covariances between attributes. 

PCA convergence is based on the method described by Gockenbach: "The rate of convergence of the power method depends on the ratio \(lambda_2|/|\lambda_1\). If this is small...then the power method converges rapidly. If the ratio is close to 1, then convergence is quite slow. The power method will fail if \(lambda_2| = |\lambda_1\)." (567). 


The objective of PCA is to maximize variance while minimizing covariance. 

To accomplish this, for a new matrix \(C_{y}\) with off diagonal entries of 0, and each successive dimension of Y ranked according to variance, PCA finds an orthonormal matrix \(P\) such that \(Y=PX\) constrained by the requirement that \(C_{y}=\frac{1}{n}YY^{T}\) be a diagonal matrix. 

The rows of \(P\) are the principal components of X.

\(C_{y}=\frac{1}{n}YY^{T}\)
\(=\frac{1}{n}(PX)(PX)^{T}\)
\(C_{y}=PC_{x}P^{T}.\)

Because any symmetric matrix is diagonalized by an orthogonal matrix of its eigenvectors, solve matrix \(P\) to be a matrix where each row is an eigenvector of 
\(\frac{1}{n}XX^{T}=C_{x}\)

Then the principal components of \(X\) are the eigenvectors of \(C_{x}\), and the \(i^{th}\) diagonal value of \(C_{y}\) is the variance of \(X\) along \(p_{i}\). 

Eigenvectors of \(C_{x}\) are found by first finding the eigenvalues \(\lambda\) of \(C_{x}\).

For each eigenvalue \(\lambda\) \((C-{x}-\lambda I)x =0\) where \(x\) is the eigenvector associated with \(\lambda\). 

Solve for \(x\) by Gaussian elimination. 

####Recovering SVD from GLRM

GLRM gives \(x\)  and \(y\), where \(x \in \rm \Bbb I \!\Bbb R ^{n * k}\) and \( y \in \rm \Bbb I \!\Bbb R ^{k*m} \)

&nbsp;&nbsp;&nbsp;- \(n\)= number of rows (A)

&nbsp;&nbsp;&nbsp;- \(m\)= number of columns (A)

&nbsp;&nbsp;&nbsp;- \(k\)= user-specified rank
&nbsp;&nbsp;&nbsp;- \(A\)= training matrix

It is assumed that the \(x\) and \(y\) columns are independent. 

First, perform QR decomposition of \(x\) and \(y^T\): 

&nbsp;&nbsp;&nbsp;\(x = QR\) 

&nbsp;&nbsp;&nbsp; \(y^T = ZS\), where \(Q^TQ = I = Z^TZ\)

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Call JAMA QR Decomposition directly on \(y^T\) to get \( Z \in \rm \Bbb I \! \Bbb R\), \( S \in \Bbb I \! \Bbb R \)

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\( R \) from QR decomposition of \( x \) is the upper triangular factor of Cholesky of \(X^TX\) Gram

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\( X^TX = LL^T, X = QR \)

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\( X^TX= (R^TQ^T) QR = R^TR \), since \(Q^TQ=I \) => \(R=L^T\) (transpose lower triangular)

**Note**: In code, \(X^TX \over n\) = \( LL^T \)

&nbsp;&nbsp;&nbsp;\( X^TX = (L \sqrt{n})(L \sqrt{n})^T =R^TR \)

&nbsp;&nbsp;&nbsp;\( R = L^T \sqrt{n} \in \rm \Bbb I \! \Bbb R^{k * k} \) reduced QR decomposition. 

For more information, refer to the [Rectangular matrix](https://en.wikipedia.org/wiki/QR_decomposition#Rectangular_matrix) section of "QR Decomposition" on Wikipedia. 

\( XY = QR(ZS)^T = Q(RS^T)Z^T \)

**Note**: \( (RS^T) \in \rm \Bbb I \!\Bbb R \)

Find SVD (locally) of \( RS^T \)

\( RS^T = U \sum V^T, U^TU = I = V^TV \) orthogonal 

\( XY = Q(RS^T)Z^T = (QU \sum (V^T Z^T) SVD \)

&nbsp;&nbsp;&nbsp;\( (QU)^T(QU) = U^T Q^TQU U^TU = I\)

&nbsp;&nbsp;&nbsp;\( (ZV)^T(ZV) = V^TZ^TZV = V^TV =I \)

Right singular vectors: \( ZV \in \rm \Bbb I \!\Bbb R^{m * k} \)

Singular values: \( \sum \in \rm \Bbb I \!\Bbb R^{k * k} \) diagonal

Left singular vectors: \( (QU) \in \rm \Bbb I \!\Bbb R^{n * k}\)



###References

Gockenbach, Mark S. "Finite-Dimensional Linear Algebra (Discrete Mathematics and Its Applications)." (2010): 566-567. 


---

<a name="GBM"></a>
##GBM

###Introduction

Gradient Boosted Regression and Gradient Boosted Classification are forward learning ensemble methods. The guiding heuristic is that good predictive results can be obtained through increasingly refined approximations. H2O's GBM sequentially builds regression trees on all the features of the dataset in a fully distributed way - each tree is built in parallel.

###Defining a GBM Model

- **Model_id**: (Optional) Enter a custom name for the model to use as a reference. By default, H2O automatically generates a destination key. 

- **Training_frame**: (Required) Select the dataset used to build the model. 
**NOTE**: If you click the **Build a model** button from the `Parse` cell, the training frame is entered automatically. 

- **Validation_frame**: (Optional) Select the dataset used to evaluate the accuracy of the model. 

- **Nfolds**: Specify the number of folds for cross-validation. 

- **Fold_column**: Select the column that contains the cross-validation fold index assignment per observation. 

- **Response_column**: (Required) Select the column to use as the independent variable.

- **Ignored_columns**: (Optional) Click the checkbox next to a column name to add it to the list of columns excluded from the model. To add all columns, click the **Select All** button. To remove a column from the list of ignored columns, click the X next to the column name. To remove all columns from the list of ignored columns, click the **Deselect All** button. To search for a specific column, type the column name in the **Search** field above the column list. To only show columns with a specific percentage of missing values, specify the percentage in the **Only show columns with more than 0% missing values** field. To change the selections for the hidden columns, use the **Select Visible** or **Deselect Visible** buttons. 

- **Ignore\_const\_cols**: Check this checkbox to ignore constant training columns, since no information can be gained from them. This option is selected by default. 

- **Offset_column**: Select a column to use as the offset. 
	>*Note*: Offsets are per-row "bias values" that are used during model training. For Gaussian distributions, they can be seen as simple corrections to the response (y) column. Instead of learning to predict the response (y-row), the model learns to predict the (row) offset of the response column. For other distributions, the offset corrections are applied in the linearized space before applying the inverse link function to get the actual response values. For more information, refer to the following [link](http://www.idg.pl/mirrors/CRAN/web/packages/gbm/vignettes/gbm.pdf). 

- **Weights_column**: Select a column to use for the observation weights, which are used for bias correction.
	>*Note*: Weights are per-row observation weights. This is typically the number of times a row is repeated, but non-integer values are supported as well. During training, rows with higher weights matter more, due to the larger loss function pre-factor.  

- **Ntrees**: Specify the number of trees.  

- **Max\_depth**: Specify the maximum tree depth.  

- **Min\_rows**: Specify the minimum number of observations for a leaf (`nodesize` in R).   

- **Nbins**: (Numerical/real/int only) Specify the number of bins for the histogram to build, then split at the best point.  

- **Nbins_cats**: (Categorical/enums only) Specify the number of bins for the histogram to build, then split at the best point. Higher values can lead to more overfitting.   

- **Seed**: Specify the random number generator (RNG) seed for algorithm components dependent on randomization. The seed is consistent for each H2O instance so that you can create models with the same starting conditions in alternative configurations. 

- **Learn_rate**: Specify the learning rate. The range is 0.0 to 1.0. 

- **Distribution**: Select the loss function. The options are auto, bernoulli, multinomial, gaussian, poisson, gamma, or tweedie.  

- **Score\_each\_iteration**: (Optional) Check this checkbox to score during each iteration of the model training. 

- **Balance_classes**: Oversample the minority classes to balance the class distribution. This option is not selected by default. This option is only applicable for classification. Majority classes can be undersampled to satisfy the **Max\_after\_balance\_size** parameter.

- **Max\_confusion\_matrix\_size**: Specify the maximum size (in number of classes) for confusion matrices to be printed in the Logs. 

- **Max\_hit\_ratio\_k**: Specify the maximum number (top K) of predictions to use for hit ratio computation. Applicable to multi-class only. To disable, enter 0. 

- **R2_stopping**: Specify a threshold for the coefficient of determination (\(r^2\)) metric value. When this threshold is met or exceeded, H2O stops making trees.   

- **Build\_tree\_one\_node**: To run on a single node, check this checkbox. This is suitable for small datasets as there is no network overhead but fewer CPUs are used.

- **Keep\_cross\_validation\_splits**: To keep the cross-validation frames, check this checkbox. 

- **Fold_assignment**: (Applicable only if a value for **nfolds** is specified and **fold_column** is not selected) Select the cross-validation fold assignment scheme. The available options are Random or Modulo. 
 
- **Class\_sampling\_factors**: Specify the per-class (in lexicographical order) over/under-sampling ratios. By default, these ratios are automatically computed during training to obtain the class balance. There is no default value. 

- **Max\_after\_balance\_size**: Specify the maximum relative size of the training data after balancing class counts (**balance\_classes** must be enabled). The value can be less than 1.0. 


###Interpreting a GBM Model

The output for GBM includes the following: 

- Model parameters (hidden)
- A graph of the scoring history (training MSE vs number of trees)
- A graph of the variable importances
- Output (model category, validation metrics, initf)
- Model summary (number of trees, min. depth, max. depth, mean depth, min. leaves, max. leaves, mean leaves)
- Scoring history in tabular format
- Training metrics (model name, model checksum name, frame name, description, model category, duration in ms, scoring time, predictions, MSE, R2)
- Variable importances in tabular format

###FAQ

- **How does the algorithm handle missing values during training?**

  Missing values do not alter the tree building in any way (i.e., they are not counted as a point when computing means or errors). Rows containing missing values do affect tree building, but the missing values don't change the split-point of the column they are in.

- **How does the algorithm handle missing values during testing?**

  During scoring, missing values "always go left" at any decision point in a tree. Due to dynamic binning in GBM, a row with a missing value typically ends up in the "leftmost bin" - with other outliers.

- **What happens if the response has missing values?**

  No errors will occur, but nothing will be learned from rows containing missing the response.

- **Does it matter if the data is sorted?** 

  No.

- **Should data be shuffled before training?**

  No.

- **How does the algorithm handle highly imbalanced data in a response column?**

  You can specify `balance_classes`, `class_sampling_factors` and `max_after_balance_size` to control over/under-sampling.

- **What if there are a large number of columns?**

  DRF models are best for datasets with fewer than a few thousand columns.

- **What if there are a large number of categorical factor levels?**

  Large number of categoricals are handled very efficiently - there is never any one-hot encoding.

###GBM Algorithm 

H2O's Gradient Boosting Algorithms follow the algorithm specified by Hastie et al (2001):


Initialize \(f_{k0} = 0,\: k=1,2,…,K\)

For \(m=1\) to \(M:\)
  
   &nbsp;&nbsp;(a) Set \(p_{k}(x)=\frac{e^{f_{k}(x)}}{\sum_{l=1}^{K}e^{f_{l}(x)}},\:k=1,2,…,K\)

   &nbsp;&nbsp;(b) For \(k=1\) to \(K\):

   &nbsp;&nbsp;&nbsp;&nbsp;i. Compute \(r_{ikm}=y_{ik}-p_{k}(x_{i}),\:i=1,2,…,N.\)
	&nbsp;&nbsp;&nbsp;&nbsp;ii. Fit a regression tree to the targets \(r_{ikm},\:i=1,2,…,N\), giving terminal regions \(R_{jim},\:j=1,2,…,J_{m}.\)
   \(iii. Compute\) \(\gamma_{jkm}=\frac{K-1}{K}\:\frac{\sum_{x_{i}\in R_{jkm}}(r_{ikm})}{\sum_{x_{i}\in R_{jkm}}|r_{ikm}|(1-|r_{ikm})},\:j=1,2,…,J_{m}.\)
	\(\:iv.\:Update\:f_{km}(x)=f_{k,m-1}(x)+\sum_{j=1}^{J_{m}}\gamma_{jkm}I(x\in\:R_{jkm}).\)	      

Output \(\:\hat{f_{k}}(x)=f_{kM}(x),\:k=1,2,…,K.\) 

Be aware that the column type affects how the histogram is created and the column type depends on whether rows are excluded or assigned a weight of 0. For example:

val weight
1      1
0.5    0
5      1
3.5    0

The above vec has a real-valued type if passed as a whole, but if the zero-weighted rows are sliced away first, the integer weight is used. The resulting histogram is either kept at full `nbins` resolution or potentially shrunk to the discrete integer range, which affects the split points. 

###References

Dietterich, Thomas G, and Eun Bae Kong. "Machine Learning Bias,
Statistical Bias, and Statistical Variance of Decision Tree
Algorithms." ML-95 255 (1995).

Elith, Jane, John R Leathwick, and Trevor Hastie. "A Working Guide to
Boosted Regression Trees." Journal of Animal Ecology 77.4 (2008): 802-813

Friedman, Jerome H. "Greedy Function Approximation: A Gradient
Boosting Machine." Annals of Statistics (2001): 1189-1232.

Friedman, Jerome, Trevor Hastie, Saharon Rosset, Robert Tibshirani,
and Ji Zhu. "Discussion of Boosting Papers." Ann. Statist 32 (2004): 
102-107

[Friedman, Jerome, Trevor Hastie, and Robert Tibshirani. "Additive
Logistic Regression: A Statistical View of Boosting (With Discussion
and a Rejoinder by the Authors)." The Annals of Statistics 28.2
(2000): 337-407](http://projecteuclid.org/DPubS?service=UI&version=1.0&verb=Display&handle=euclid.aos/1016218223)

[Hastie, Trevor, Robert Tibshirani, and J Jerome H Friedman. The
Elements of Statistical Learning.
Vol.1. N.p., page 339: Springer New York, 2001.](http://www.stanford.edu/~hastie/local.ftp/Springer/OLD//ESLII_print4.pdf)

---

<a name="DL"></a>
##Deep Learning

###Introduction

H2O’s Deep Learning is based on a multi-layer feed-forward artificial neural network that is trained with stochastic gradient descent using back-propagation. The network can contain a large number of hidden layers consisting of neurons with tanh, rectifier and maxout activation functions. Advanced features such as adaptive learning rate, rate annealing, momentum training, dropout, L1 or L2 regularization, checkpointing and grid search enable high predictive accuracy. Each compute node trains a copy of the global model parameters on its local data with multi-threading (asynchronously), and contributes periodically to the global model via model averaging across the network.

###Defining a Deep Learning Model

H2O Deep Learning models have many input parameters, many of which are only accessible via the expert mode. For most cases, use the default values. Please read the following instructions before building extensive Deep Learning models. The application of grid search and successive continuation of winning models via checkpoint restart is highly recommended, as model performance can vary greatly.

- **Model_id**: (Optional) Enter a custom name for the model to use as a reference. By default, H2O automatically generates a destination key. 

- **Training_frame**: (Required) Select the dataset used to build the model. 
**NOTE**: If you click the **Build a model** button from the `Parse` cell, the training frame is entered automatically. 

- **Validation_frame**: (Optional) Select the dataset used to evaluate the accuracy of the model. 

- **Nfolds**: Specify the number of folds for cross-validation. 

- **Fold_column**: Select the column that contains the cross-validation fold index assignment per observation. 

- **Response_column**: Select the column to use as the independent variable.

- **Ignored_columns**: (Optional) Click the checkbox next to a column name to add it to the list of columns excluded from the model. To add all columns, click the **Select All** button. To remove a column from the list of ignored columns, click the X next to the column name. To remove all columns from the list of ignored columns, click the **Deselect All** button. To search for a specific column, type the column name in the **Search** field above the column list. To only show columns with a specific percentage of missing values, specify the percentage in the **Only show columns with more than 0% missing values** field. To change the selections for the hidden columns, use the **Select Visible** or **Deselect Visible** buttons. 

- **Ignore\_const\_cols**: Check this checkbox to ignore constant training columns, since no information can be gained from them. This option is selected by default. 

- **Weights_column**: Select a column to use for the observation weights, which are used for bias correction.
	>*Note*: Weights are per-row observation weights. This is typically the number of times a row is repeated, but non-integer values are supported as well. During training, rows with higher weights matter more, due to the larger loss function pre-factor.  

- **Offset_column**: Select a column to use as the offset. 
	>*Note*: Offsets are per-row "bias values" that are used during model training. For Gaussian distributions, they can be seen as simple corrections to the response (y) column. Instead of learning to predict the response (y-row), the model learns to predict the (row) offset of the response column. For other distributions, the offset corrections are applied in the linearized space before applying the inverse link function to get the actual response values. For more information, refer to the following [link](http://www.idg.pl/mirrors/CRAN/web/packages/gbm/vignettes/gbm.pdf). 

- **Activation**: Select the activation function (Tahn, Tahn with dropout, Rectifier, Rectifier with dropout, Maxout, Maxout with dropout).   

- **Hidden**: Specify the hidden layer sizes (e.g., 100,100).  

- **Epochs**: Specify the number of times to iterate (stream) the dataset. The value can be a fraction.   

- **Variable_importances**: Check this checkbox to compute variable importance. This option is not selected by default. 

- **Balance_classes**: Oversample the minority classes to balance the class distribution. This option is not selected by default. This option is only applicable for classification. Majority classes can be undersampled to satisfy the **Max\_after\_balance\_size** parameter. 

- **Max\_confusion\_matrix\_size**: Specify the maximum size (in number of classes) for confusion matrices to be printed in the Logs. 

- **Max\_hit\_ratio\_k**: Specify the maximum number (top K) of predictions to use for hit ratio computation. Applicable to multi-class only. To disable, enter 0. 

- **Checkpoint**: Enter a model key associated with a previously-trained Deep Learning model. Use this option to build a new model as a continuation of a previously-generated model.

- **Use\_all\_factor\_levels**: Check this checkbox to use all factor levels in the possible set of predictors; if you enable this option, sufficient regularization is required. By default, the first factor level is skipped. For Deep Learning models, this option is useful for determining variable importances and is automatically enabled if the autoencoder is selected. 

- **Train\_samples\_per\_iteration**: Specify the number of global training samples per MapReduce iteration. To specify one epoch, enter 0. To specify all available data (e.g., replicated training data), enter -1. To use the automatic values, enter -2.   

- **Adaptive_rate**: Check this checkbox to enable the adaptive learning rate (ADADELTA). This option is selected by default. 

- **Input\_dropout\_ratio**: Specify the input layer dropout ratio to improve generalization. Suggested values are 0.1 or 0.2.  

- **L1**: Specify the L1 regularization to add stability and improve generalization; sets the value of many weights to 0. 

- **L2**: Specify the L2 regularization to add stability and improve generalization; sets the value of many weights to smaller values. 

- **Loss**:  Select the loss function. The options are automatic, mean square, cross-entropy, Huber, or Absolute and the default value is automatic. 

- **Score_interval**: Specify the shortest time interval (in seconds) to wait between model scoring.  

- **Score\_training\_samples**: Specify the number of training set samples for scoring. To use all training samples, enter 0.  

- **Score\_duty\_cycle**: Specify the maximum duty cycle fraction for scoring. A lower value results in more training and a higher value results in more scoring. 

- **Autoencoder**: Check this checkbox to enable the Deep Learning autoencoder. This option is not selected by default. **Note**: This option requires **MeanSquare** as the loss function. 

- **Keep\_cross\_validation\_splits**: To keep the cross-validation frames, check this checkbox. 

- **Fold_assignment**: (Applicable only if a value for **nfolds** is specified and **fold_column** is not selected) Select the cross-validation fold assignment scheme. The available options are Random or Modulo. 

- **Class\_sampling\_factors**: Specify the per-class (in lexicographical order) over/under-sampling ratios. By default, these ratios are automatically computed during training to obtain the class balance.  

- **Max\_after\_balance\_size**: Specify the maximum relative size of the training data after balancing class counts (**balance\_classes** must be enabled). The value can be less than 1.0. 

- **Overwrite\_with\_best\_model**: Check this checkbox to overwrite the final model with the best model found during training. This option is selected by default. 

- **Target\_ratio\_comm\_to\_comp**: Specify the target ratio of communication overhead to computation. This option is only enabled for multi-node operation and if **train\_samples\_per\_iteration** equals -2 (auto-tuning).  

- **Seed**: Specify the random number generator (RNG) seed for algorithm components dependent on randomization. The seed is consistent for each H2O instance so that you can create models with the same starting conditions in alternative configurations. 

- **Rho**: Specify the adaptive learning rate time decay factor.   

- **Epsilon**: Specify the adaptive learning rate time smoothing factor to avoid dividing by zero. 

- **Max_W2**: Specify the constraint for the squared sum of the incoming weights per unit (e.g., for Rectifier).  

- **Initial\_weight\_distribution**: Select the initial weight distribution (Uniform Adaptive, Uniform, or Normal).   

- **Regression_stop**: Specify the stopping criterion for regression error (MSE) on the training data. To disable this option, enter -1.  

- **Diagnostics**: Check this checkbox to compute the variable importances for input features (using the Gedeon method). For large networks, selecting this option can reduce speed. This option is selected by default. 

- **Fast_mode**: Check this checkbox to enable fast mode, a minor approximation in back-propagation. This option is selected by default. 

- **Force\_load\_balance**: Check this checkbox to force extra load balancing to increase training speed for small datasets and use all cores. This option is selected by default. 

- **Single\_node\_mode**: Check this checkbox to force H2O to run on a single node for fine-tuning of model parameters. This option is not selected by default. 

- **Shuffle\_training\_data**: Check this checkbox to shuffle the training data. This option is recommended if the training data is replicated and the value of **train\_samples\_per\_iteration** is close to the number of nodes times the number of rows. This option is not selected by default. 

- **Missing\_values\_handling**: Select how to handle missing values (skip or mean imputation).   

- **Quiet_mode**: Check this checkbox to display less output in the standard output. This option is not selected by default. 

- **Sparse**: Check this checkbox to use sparse data handling. This option is not selected by default. 

- **Col_major**: Check this checkbox to use a column major weight matrix for the input layer. This option can speed up forward propagation but may reduce the speed of backpropagation. This option is not selected by default. 

- **Average_activation**: Specify the average activation for the sparse autoencoder.  

- **Sparsity_beta**: Specify the sparsity regularization.   

- **Max\_categorical\_features**: Specify the maximum number of categorical features enforced via hashing.

- **Reproducible**: To force reproducibility on small data, check this checkbox. If this option is enabled, the model takes more time to generate, since it uses only one thread. 

- **Export\_weights\_and\_biases**: To export the neural network weights and biases as H2O frames, check this checkbox. 


###Interpreting a Deep Learning Model

To view the results, click the View button. The output for the Deep Learning model includes the following information for both the training and testing sets: 

- Model parameters (hidden)
- A chart of the variable importances
- A graph of the scoring history (training MSE and validation MSE vs epochs)
- Output (model category, weights, biases)
- Status of neuron layers (layer number, units, type, dropout, L1, L2, mean rate, rate RMS, momentum, mean weight, weight RMS, mean bias, bias RMS)
- Scoring history in tabular format
- Training metrics (model name, model checksum name, frame name, frame checksum name, description, model category, duration in ms, scoring time, predictions, MSE, R2, logloss)
- Top-K Hit Ratios (for multi-class classification)
- Confusion matrix (for classification)



###FAQ

- **How does the algorithm handle missing values during training?**

  User-specifiable treatment of missing values via `missing_values_handling`. Specify either the skip or mean-impute option.

- **How does the algorithm handle missing values during testing?**

  Missing values in the test set will be mean-imputed during scoring.

- **What happens if the response has missing values?**

  No errors will occur, but nothing will be learned from rows containing missing the response.

- **Does it matter if the data is sorted?** 

  Yes, since the training set is processed in order. Depending whether `train_samples_per_iteration` is enabled, some rows will be skipped. If `shuffle_training_data` is enabled, then each thread that is processing a small subset of rows will process rows randomly, but it is not a global shuffle.

- **Should data be shuffled before training?**

  Yes, the data should be shuffled before training, especially if the dataset is sorted. 

- **How does the algorithm handle highly imbalanced data in a response column?**

  Specify `balance_classes`, `class_sampling_factors` and `max_after_balance_size` to control over/under-sampling.

- **What if there are a large number of columns?**

  The input neuron layer's size is scaled to the number of input features, so as the number of columns increases, the model complexity increases as well. 
  
- **What if there are a large number of categorical factor levels?**

This is something to look out for. Say you have three columns: zip code (70k levels), height, and income. The resulting number of internally one-hot encoded features will be 70,002 and only 3 of them will be activated (non-zero). If the first hidden layer has 200 neurons, then the resulting weight matrix will be of size 70,002 x 200, which can take a long time to train and converge. In this case, we recommend either reducing the number of categorical factor levels upfront (e.g., using `h2o.interaction()` from R), or specifying `max_categorical_features` to use feature hashing to reduce the dimensionality.

###Deep Learning Algorithm 

For more information about how the Deep Learning algorithm works, refer to the [Deep Learning booklet](https://leanpub.com/deeplearning/read). 

###References

 ["Deep Learning." *Wikipedia: The free encyclopedia*. Wikimedia Foundation, Inc. 1 May 2015. Web. 4 May 2015.](http://en.wikipedia.org/wiki/Deep_learning)

 ["Artificial Neural Network." *Wikipedia: The free encyclopedia*. Wikimedia Foundation, Inc. 22 April 2015. Web. 4 May 2015.](http://en.wikipedia.org/wiki/Artificial_neural_network)

 [Zeiler, Matthew D. 'ADADELTA: An Adaptive Learning Rate Method'. Arxiv.org. N.p., 2012. Web. 4 May 2015.](http://arxiv.org/abs/1212.5701)

 [Sutskever, Ilya et al. "On the importance of initialization and momementum in deep learning." JMLR:W&CP vol. 28. (2013).](http://www.cs.toronto.edu/~fritz/absps/momentum.pdf)

 [Hinton, G.E. et. al. "Improving neural networks by preventing co-adaptation of feature detectors." University of Toronto. (2012).](http://arxiv.org/pdf/1207.0580.pdf)

 [Wager, Stefan et. al. "Dropout Training as Adaptive Regularization." Advances in Neural Information Processing Systems. (2013).](http://arxiv.org/abs/1307.1493)

 [Gedeon, TD. "Data mining of inputs: analysing magnitude and functional measures." University of New South Wales. (1997).](http://www.ncbi.nlm.nih.gov/pubmed/9327276)
    
 [Candel, Arno and Parmar, Viraj. "Deep Learning with H2O." H2O.ai, Inc. (2015).](https://leanpub.com/deeplearning)
    
  [Deep Learning Training](http://learn.h2o.ai/content/hands-on_training/deep_learning.html)
    
  [Slideshare slide decks](http://www.slideshare.net/0xdata/presentations?order=latest)
    
  [Youtube channel](https://www.youtube.com/user/0xdata)
    
  [Candel, Arno. "The Definitive Performance Tuning Guide for H2O Deep Learning." H2O.ai, Inc. (2015).](http://h2o.ai/blog/2015/02/deep-learning-performance/)

  [Niu, Feng, et al. "Hogwild!: A lock-free approach to parallelizing stochastic gradient descent." Advances in Neural Information Processing Systems 24 (2011): 693-701. (algorithm implemented is on p.5)](https://papers.nips.cc/paper/4390-hogwild-a-lock-free-approach-to-parallelizing-stochastic-gradient-descent.pdf)

  [Hawkins, Simon et al. "Outlier Detection Using Replicator Neural Networks." CSIRO Mathematical and Information Sciences](http://neuro.bstu.by/ai/To-dom/My_research/Paper-0-again/For-research/D-mining/Anomaly-D/KDD-cup-99/NN/dawak02.pdf)

