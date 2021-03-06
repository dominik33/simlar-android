/**
 * Copyright (C) 2013 The Simlar Authors.
 *
 * This file is part of Simlar. (http://www.simlar.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.simlar;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class SimlarServiceBroadcast implements Serializable
{
	private static final long serialVersionUID = -7496021571744376633L;

	public static final String BROADCAST_NAME = "SimlarServiceBroadcast";
	public static final String INTENT_EXTRA = "SimlarServiceBroadcast";

	public enum Type {
		SIMLAR_STATUS,
		PRESENCE_STATE,
		SIMLAR_CALL_STATE,
		SERVICE_FINISHES,
		TEST_REGISTRATION_SUCCESS,
		TEST_REGISTRATION_FAILED;
	}

	public final Type type;
	public final Parameters parameters;

	private SimlarServiceBroadcast(final Type type, final Parameters parameters)
	{
		this.type = type;
		this.parameters = parameters;
	}

	private void send(final Context context)
	{
		Intent intent = new Intent(BROADCAST_NAME);
		intent.putExtra(INTENT_EXTRA, this);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	public static interface Parameters extends Serializable
	{
	}

	public static void sendSimlarStatusChanged(final Context context)
	{
		new SimlarServiceBroadcast(Type.SIMLAR_STATUS, null).send(context);
	}

	public static class PresenceStateChanged implements Parameters
	{
		private static final long serialVersionUID = 2863651982137778543L;

		public final String number;
		public final boolean online;

		public PresenceStateChanged(final String number, final boolean online)
		{
			this.number = number;
			this.online = online;
		}
	}

	public static void sendPresenceStateChanged(final Context context, final String number, final boolean online)
	{
		new SimlarServiceBroadcast(Type.PRESENCE_STATE, new PresenceStateChanged(number, online)).send(context);
	}

	public static void sendSimlarCallStateChanged(final Context context)
	{
		new SimlarServiceBroadcast(Type.SIMLAR_CALL_STATE, null).send(context);
	}

	public static void sendServiceFinishes(final Context context)
	{
		new SimlarServiceBroadcast(Type.SERVICE_FINISHES, null).send(context);
	}

	public static void sendTestRegistrationSuccess(final Context context)
	{
		new SimlarServiceBroadcast(Type.TEST_REGISTRATION_SUCCESS, null).send(context);
	}

	public static void sendTestRegistrationFailed(final Context context)
	{
		new SimlarServiceBroadcast(Type.TEST_REGISTRATION_FAILED, null).send(context);
	}
}
