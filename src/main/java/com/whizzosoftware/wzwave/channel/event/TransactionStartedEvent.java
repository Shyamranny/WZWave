/*
 *******************************************************************************
 * Copyright (c) 2016 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.channel.event;

/**
 * A user event that indicates a Z-Wave data frame transaction has started.
 *
 * @author Dan Noguerol
 */
public class TransactionStartedEvent {
    private String id;

    public TransactionStartedEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString()
    {
        return "TransactionStartedEvent{" +
                "id=" + getId() +
                '}';
    }
}
