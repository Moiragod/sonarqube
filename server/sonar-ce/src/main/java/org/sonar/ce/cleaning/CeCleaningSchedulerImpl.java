/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.ce.cleaning;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.ce.configuration.CeConfiguration;
import org.sonar.ce.queue.InternalCeQueue;

import static java.util.concurrent.TimeUnit.MINUTES;

public class CeCleaningSchedulerImpl implements CeCleaningScheduler {
  private static final Logger LOG = Loggers.get(CeCleaningSchedulerImpl.class);

  private final CeCleaningExecutorService executorService;
  private final CeConfiguration ceConfiguration;
  private final InternalCeQueue internalCeQueue;

  public CeCleaningSchedulerImpl(CeCleaningExecutorService executorService, CeConfiguration ceConfiguration, InternalCeQueue internalCeQueue) {
    this.executorService = executorService;
    this.internalCeQueue = internalCeQueue;
    this.ceConfiguration = ceConfiguration;
  }

  @Override
  public void startScheduling() {
    executorService.scheduleWithFixedDelay(this::cancelWornOuts,
      ceConfiguration.getCancelWornOutsInitialDelay(),
      ceConfiguration.getCancelWornOutsDelay(),
      MINUTES);
  }

  private void cancelWornOuts() {
    try {
      LOG.info("Deleting any worn out task");
      internalCeQueue.cancelWornOuts();
    } catch (Exception e) {
      LOG.warn("Failed to cancel worn out tasks", e);
    }
  }
}