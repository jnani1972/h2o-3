##################################
## basic regression test
##################################


setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source('../../h2o-runit.R')

test.regression.basic <- function(conn) {
  cars.hex <- h2o.uploadFile(conn, locate("smalldata/junit/cars.csv"))
  cars.hex[,3] <- as.factor(cars.hex[,3])

  cars.drf <- h2o.randomForest(x = 3:7, y = 2, cars.hex)
  print(cars.drf)

  testEnd()
}

doTest("Basic Regession using Random Forest", test.regression.basic)
