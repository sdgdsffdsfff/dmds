/*
 * Copyright (C) 2016-2020 zhongan.com
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.zhongan.dmds.net.sqlengine;

/**
 * called when all jobs in EngineCxt finished
 *
 * @author wuzhih
 */
public interface AllJobFinishedListener {

  void onAllJobFinished(EngineCtx ctx);
}
