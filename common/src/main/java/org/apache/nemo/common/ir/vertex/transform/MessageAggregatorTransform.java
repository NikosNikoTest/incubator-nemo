/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.nemo.common.ir.vertex.transform;

import org.apache.nemo.common.Pair;
import org.apache.nemo.common.ir.OutputCollector;
import org.apache.nemo.common.ir.vertex.utility.runtimepass.MessageAggregatorVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Transform} that aggregates statistics generated by the {@link MessageGeneratorTransform}.
 *
 * @param <K> input key type.
 * @param <V> input value type.
 * @param <O> output type.
 */
public final class MessageAggregatorTransform<K, V, O> extends NoWatermarkEmitTransform<Pair<K, V>, O> {
  private static final Logger LOG = LoggerFactory.getLogger(MessageAggregatorTransform.class.getName());

  private transient O state;
  private transient OutputCollector<O> outputCollector;

  private final MessageAggregatorVertex.InitialStateSupplier<O> initialStateSupplier;
  private final MessageAggregatorVertex.MessageAggregatorFunction<K, V, O> aggregator;

  /**
   * Default constructor.
   * @param initialStateSupplier to use.
   * @param aggregator to use.
   */
  public MessageAggregatorTransform(final MessageAggregatorVertex.InitialStateSupplier<O> initialStateSupplier,
                                    final MessageAggregatorVertex.MessageAggregatorFunction<K, V, O> aggregator) {
    this.initialStateSupplier = initialStateSupplier;
    this.aggregator = aggregator;
  }

  @Override
  public void prepare(final Context context, final OutputCollector<O> oc) {
    this.state = initialStateSupplier.get();
    this.outputCollector = oc;
  }

  @Override
  public void onData(final Pair<K, V> element) {
    state = aggregator.apply(element, state);
  }

  @Override
  public void close() {
    outputCollector.emit(state);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(MessageAggregatorTransform.class);
    sb.append(":");
    sb.append(super.toString());
    return sb.toString();
  }
}
