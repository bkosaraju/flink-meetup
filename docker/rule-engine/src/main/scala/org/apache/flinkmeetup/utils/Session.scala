package org.apache.flinkmeetup.utils

import org.slf4j.LoggerFactory


trait Session extends Serializable {
  def logger = LoggerFactory.getLogger(getClass)
}