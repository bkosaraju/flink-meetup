package main.org.apache.flinkmeetup

trait DefaultConfig {
  lazy val DEFAULT_CHECKPOINT_INTERVAL = 1000 //defaultCheckpointInterval
  lazy val MIN_PAUSE_CHECKPOINT_INTERVAL = 500 //minPauseBetweenCheckPoints
  lazy val CHECK_POINT_TIMEOUT = 60000 //checkpointTimeout
  lazy val TOLERABLE_CHECKPOINT_FALIRUS = 3 //tolerableCheckpointFailureNumber
  lazy val MAX_CONCURRENT_CHECKPOINTS = 1 //maxConcurrentCheckpoints
  lazy val CHECKPOINT_STORAGE = "file:///tmp/flink/checkpoint" //checkpointStorageLocation
}
