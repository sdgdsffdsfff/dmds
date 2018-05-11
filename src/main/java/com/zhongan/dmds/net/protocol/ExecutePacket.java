/*
 * Copyright (C) 2016-2020 zhongan.com
 * based on code by MyCATCopyrightHolder Copyright (c) 2013, OpenCloudDB/MyCAT.
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher.
 */
package com.zhongan.dmds.net.protocol;

import com.zhongan.dmds.commons.mysql.BindValue;
import com.zhongan.dmds.commons.mysql.MySQLMessage;
import com.zhongan.dmds.net.mysql.BindValueUtil;
import com.zhongan.dmds.net.mysql.PreparedStatement;

import java.io.UnsupportedEncodingException;

/**
 * <pre>
 *  Bytes                      Name
 *  -----                      ----
 *  1                          code
 *  4                          statement_id
 *  1                          flags
 *  4                          iteration_count
 *  (param_count+7)/8          null_bit_map
 *  1                          new_parameter_bound_flag (if new_params_bound == 1:)
 *  n*2                        type of parameters
 *  n                          values for the parameters
 *  --------------------------------------------------------------------------------
 *  code:                      always COM_EXECUTE
 *
 *  statement_id:              statement identifier
 *
 *  flags:                     reserved for future use. In MySQL 4.0, always 0.
 *                             In MySQL 5.0:
 *                               0: CURSOR_TYPE_NO_CURSOR
 *                               1: CURSOR_TYPE_READ_ONLY
 *                               2: CURSOR_TYPE_FOR_UPDATE
 *                               4: CURSOR_TYPE_SCROLLABLE
 *
 *  iteration_count:           reserved for future use. Currently always 1.
 *
 *  null_bit_map:              A bitmap indicating parameters that are NULL.
 *                             Bits are counted from LSB, using as many bytes
 *                             as necessary ((param_count+7)/8)
 *                             i.e. if the first parameter (parameter 0) is NULL, then
 *                             the least significant bit in the first byte will be 1.
 *
 *  new_parameter_bound_flag:  Contains 1 if this is the first time
 *                             that "execute" has been called, or if
 *                             the parameters have been rebound.
 *
 *  type:                      Occurs once for each parameter;
 *                             The highest significant bit of this 16-bit value
 *                             encodes the unsigned property. The other 15 bits
 *                             are reserved for the type (only 8 currently used).
 *                             This block is sent when parameters have been rebound
 *                             or when a prepared statement is executed for the
 *                             first time.
 *
 *  values:                    for all non-NULL values, each parameters appends its value
 *                             as described in Row Data Packet: Binary (column values)
 * &#64;see http://dev.mysql.com/doc/internals/en/execute-packet.html
 * </pre>
 */
public class ExecutePacket extends MySQLPacket {

  public byte code;
  public long statementId;
  public byte flags;
  public long iterationCount;
  public byte[] nullBitMap;
  public byte newParameterBoundFlag;
  public BindValue[] values;
  protected PreparedStatement pstmt;

  public ExecutePacket(PreparedStatement pstmt) {
    this.pstmt = pstmt;
    this.values = new BindValue[pstmt.getParametersNumber()];
  }

  public void read(byte[] data, String charset) throws UnsupportedEncodingException {
    MySQLMessage mm = new MySQLMessage(data);
    packetLength = mm.readUB3();
    packetId = mm.read();
    code = mm.read();
    statementId = mm.readUB4();
    flags = mm.read();
    iterationCount = mm.readUB4();

    // 读取NULL指示器数据
    int parameterCount = values.length;
    nullBitMap = new byte[(parameterCount + 7) / 8];
    for (int i = 0; i < nullBitMap.length; i++) {
      nullBitMap[i] = mm.read();
    }

    // 当newParameterBoundFlag==1时，更新参数类型。
    newParameterBoundFlag = mm.read();
    if (newParameterBoundFlag == (byte) 1) {
      for (int i = 0; i < parameterCount; i++) {
        pstmt.getParametersType()[i] = mm.readUB2();
      }
    }

    // 设置参数类型和读取参数值
    byte[] nullBitMap = this.nullBitMap;
    for (int i = 0; i < parameterCount; i++) {
      BindValue bv = new BindValue();
      bv.type = pstmt.getParametersType()[i];
      if ((nullBitMap[i / 8] & (1 << (i & 7))) != 0) {
        bv.isNull = true;
      } else {
        BindValueUtil.read(mm, bv, charset);
      }
      values[i] = bv;
    }
  }

  @Override
  public int calcPacketSize() {

    return 0;
  }

  @Override
  protected String getPacketInfo() {
    return "MySQL Execute Packet";
  }

}