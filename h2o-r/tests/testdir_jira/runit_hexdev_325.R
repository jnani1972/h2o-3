setwd(normalizePath(dirname(R.utils::commandArgs(asValues=TRUE)$"f")))
source('../h2o-runit.R')

test.parse.mismatching.col.length <- function(conn){

  df <- h2o.importFile(locate("smalldata/jira/hexdev_325.csv"), header = TRUE)
  expected <- c("C3", "Cats", "C3C3", "C4", "Mouse", "C6")
  actual <- colnames(df)

  expect_equal(expected, actual)

  testEnd()
}

doTest("Testing Parsing of Mismatching Header and Data length", test.parse.mismatching.col.length)
