/*
 * Copyright (C) 2016-2020 zhongan.com
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.zhongan.dmds.exception;

/**
 * 未知字符集异常
 */
public class UnknownCharsetException extends RuntimeException {

  private static final long serialVersionUID = 552833416065882969L;

  public UnknownCharsetException() {
    super();
  }

  public UnknownCharsetException(String message, Throwable cause) {
    super(message, cause);
  }

  public UnknownCharsetException(String message) {
    super(message);
  }

  public UnknownCharsetException(Throwable cause) {
    super(cause);
  }

}