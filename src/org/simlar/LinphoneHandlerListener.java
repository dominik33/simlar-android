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

import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneCore.RegistrationState;

public interface LinphoneHandlerListener
{
	public void onRegistrationStateChanged(final RegistrationState state);

	public void onPresenceStateChanged(final String number, final boolean online);

	public void onCallStatsChanged(final float upload, final float download, final float quality, final String codec, final String iceState);

	public void onCallStateChanged(final String number, final State state, final int msgId);

	public void onCallEncryptionChanged(final boolean encrypted, final String authenticationToken, final boolean authenticationTokenVerified);

	public void onJoin();
}
