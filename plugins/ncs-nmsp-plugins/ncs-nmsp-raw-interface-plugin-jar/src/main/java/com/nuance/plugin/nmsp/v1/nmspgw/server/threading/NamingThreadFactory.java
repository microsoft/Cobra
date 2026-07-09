/* 
 * ---------------------------------------------------------------------------
 *
 * COPYRIGHT (c) 2015 Nuance Communications Inc. All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Nuance Communications Inc. The program(s) may be used and/or copied
 * only with the written permission from Nuance Communications Inc.
 * or in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Author: bertrand_braschi
 * Date  : Mar 13, 2015
 *
 * ---------------------------------------------------------------------------
 */
package com.nuance.plugin.nmsp.v1.nmspgw.server.threading;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class NamingThreadFactory implements ThreadFactory {

  private final AtomicInteger counter;
  private final String name;
  
  public NamingThreadFactory(final String name) {
    this.name = name;
    this.counter = new AtomicInteger(0);
  }
  
  @Override
  public Thread newThread(final Runnable r) {
    final String threadName = name + '-' + counter.incrementAndGet();
    return new Thread (r, threadName);
  }
}

