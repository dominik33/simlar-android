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

import java.text.DecimalFormat;

import org.linphone.core.LinphoneCall.State;

import android.util.Log;

public class SimlarCallState
{
	private static final String LOGTAG = SimlarCallState.class.getSimpleName();
	private static final DecimalFormat GUI_VALUE = new DecimalFormat("#0.0");

	private String mDisplayName = null;
	private org.linphone.core.LinphoneCall.State mLinphoneCallState = null;
	private int mMsgId = -1;
	private boolean mEncrypted = true;
	private String mAuthenticationToken = null;
	private boolean mAuthenticationTokenVerified = false;

	private float mUpload = -1.0f;
	private float mDownload = -1.0f;
	private float mQuality = -1.0f;
	private String mCodec = null;
	private String mIceState = null;

	public boolean updateCallStateChanged(final String displayName, final State callState, final int msgId)
	{
		if (Util.equalString(displayName, mDisplayName) && equalCallState(callState, mLinphoneCallState) && mMsgId == msgId) {
			return false;
		}

		if (callState == null) {
			Log.e(LOGTAG, "ERROR updateCallStateChanged: callState not set");
		}

		if (!Util.isNullOrEmpty(displayName)) {
			mDisplayName = displayName;
		}

		mLinphoneCallState = callState;
		mMsgId = msgId;

		if (isNewCall()) {
			mEncrypted = true;
			mAuthenticationToken = null;
			mAuthenticationTokenVerified = false;

			mUpload = -1.0f;
			mDownload = -1.0f;
			mQuality = -1.0f;
			mCodec = null;
			mIceState = null;
		}

		return true;
	}

	private static boolean equalCallState(final State lhs, final State rhs)
	{
		if (rhs == null && lhs == null) {
			return true;
		}

		if (rhs == null || lhs == null) {
			return false;
		}

		return lhs.value() == rhs.value();
	}

	public boolean updateCallStats(final float upload, final float download, final float quality, final String codec, final String iceState)
	{
		if (upload == mUpload && download == mDownload && quality == mQuality
				&& Util.equalString(codec, mCodec) && Util.equalString(iceState, mIceState)) {
			return false;
		}

		mUpload = upload;
		mDownload = download;
		mQuality = quality;
		mCodec = codec;
		mIceState = iceState;
		return true;
	}

	public boolean updateCallEncryption(final boolean encrypted, final String authenticationToken, final boolean authenticationTokenVerified)
	{
		if (encrypted == mEncrypted && authenticationTokenVerified == mAuthenticationTokenVerified
				&& Util.equalString(authenticationToken, mAuthenticationToken)) {
			return false;
		}

		mEncrypted = encrypted;
		mAuthenticationToken = authenticationToken;
		mAuthenticationTokenVerified = authenticationTokenVerified;

		return true;
	}

	public boolean isEmpty()
	{
		return mLinphoneCallState == null;
	}

	private String formatMsgId()
	{
		if (mMsgId > 0) {
			return " mMsgId=" + mMsgId;
		}

		return "";
	}

	private String formatEncryption()
	{
		if (!mEncrypted) {
			return " NOT ENCRYPTED";
		}

		return " SAS=" + mAuthenticationToken + (mAuthenticationTokenVerified ? " (verified)" : " (not verified)");
	}

	private String formatCodec()
	{
		if (Util.isNullOrEmpty(mCodec)) {
			return "";
		}

		return " Codec=" + mCodec;
	}

	private String formatIceState()
	{
		if (Util.isNullOrEmpty(mAuthenticationToken)) {
			return "";
		}

		return " IceState=" + mIceState;
	}

	private static String formatValue(final String name, final float value)
	{
		if (value <= 0) {
			return "";
		}

		return " " + name + "=" + String.valueOf(value);
	}

	@Override
	public String toString()
	{
		if (isEmpty()) {
			return "";
		}
		return "[" + mLinphoneCallState.toString() + "] " + mDisplayName + formatMsgId() + formatEncryption() + formatIceState() + formatCodec()
				+ formatValue("upload", mUpload) + formatValue("download", mDownload) + formatValue("quality", mQuality);
	}

	public String getDisplayName()
	{
		return mDisplayName;
	}

	public int getMsgId()
	{
		return mMsgId;
	}

	public boolean isEncrypted()
	{
		return mEncrypted;
	}

	public String getAuthenticationToken()
	{
		return mAuthenticationToken;
	}

	public String getUpload()
	{
		return GUI_VALUE.format(mUpload);
	}

	public String getDownload()
	{
		return GUI_VALUE.format(mDownload);
	}

	public String getQuality()
	{
		return GUI_VALUE.format(mQuality);
	}

	public int getQualityDescription()
	{
		if (4 <= mQuality && mQuality <= 5) {
			return R.string.call_activity_quality_good;
		}

		if (3 <= mQuality && mQuality < 4) {
			return R.string.call_activity_quality_average;
		}

		if (2 <= mQuality && mQuality < 3) {
			return R.string.call_activity_quality_poor;
		}

		if (1 <= mQuality && mQuality < 2) {
			return R.string.call_activity_quality_very_poor;
		}

		if (0 <= mQuality && mQuality < 1) {
			return R.string.call_activity_quality_unusable;
		}

		Log.e(LOGTAG, "unknown quality");
		return R.string.call_activity_quality_unknown;
	}

	public String getCodec()
	{
		return mCodec;
	}

	public String getIceState()
	{
		return mIceState;
	}

	public boolean isAuthenticationTokenVerified()
	{
		return mAuthenticationTokenVerified;
	}

	public boolean isTalking()
	{
		if (isEmpty()) {
			return false;
		}

		return mLinphoneCallState == State.Connected ||
				mLinphoneCallState == State.StreamsRunning ||
				mLinphoneCallState == State.CallUpdatedByRemote ||
				mLinphoneCallState == State.CallUpdating;
	}

	public boolean isNewCall()
	{
		if (isEmpty()) {
			return false;
		}

		return mLinphoneCallState == State.OutgoingInit || mLinphoneCallState == State.IncomingReceived;
	}

	public boolean isEndedCall()
	{
		if (isEmpty()) {
			return false;
		}

		return mLinphoneCallState == State.CallEnd;
	}

	public boolean isRinging()
	{
		if (isEmpty()) {
			return false;
		}

		return mLinphoneCallState == State.IncomingReceived;
	}

	public boolean hasMessage()
	{
		return mMsgId > 0;
	}

	public boolean hasConnectionInfo()
	{
		if (mUpload < 0 || mDownload < 0 || mQuality < 0) {
			return false;
		}

		if (Util.isNullOrEmpty(mCodec) || Util.isNullOrEmpty(mIceState)) {
			return false;
		}

		return true;
	}
}
