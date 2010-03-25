/*
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.kaleo.runtime.notification;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.workflow.kaleo.definition.ActionType;
import com.liferay.portal.workflow.kaleo.model.KaleoNotification;
import com.liferay.portal.workflow.kaleo.model.KaleoNotificationRecipient;
import com.liferay.portal.workflow.kaleo.runtime.ExecutionContext;
import com.liferay.portal.workflow.kaleo.service.KaleoNotificationLocalServiceUtil;
import com.liferay.portal.workflow.kaleo.service.KaleoNotificationRecipientLocalServiceUtil;

import java.util.List;

/**
 * <a href="NotificationUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author Michael C. Han
 */
public class NotificationUtil {
	
	public static void sendKaleoNotifications(
			long kaleoNodeId, ActionType actionType,
			ExecutionContext executionContext)
		throws PortalException, SystemException {

		List<KaleoNotification> kaleoNotifications =
			KaleoNotificationLocalServiceUtil.getKaleoNotifications(
				kaleoNodeId, actionType.getValue());

		for (KaleoNotification kaleoNotification : kaleoNotifications) {
			_sendKaleoNotification(kaleoNotification, executionContext);
		}
	}

	private static void _sendKaleoNotification(
			KaleoNotification kaleoNotification,
			ExecutionContext executionContext)
		throws PortalException, SystemException {

		NotificationMessageGenerator notificationMessageGenerator =
			NotificationMessageGeneratorFactory.getNotificationMessageGenerator(
				kaleoNotification.getLanguage());

		String notificationMessage =
			notificationMessageGenerator.generateMessage(
				kaleoNotification.getKaleoNodeId(), kaleoNotification.getName(),
				kaleoNotification.getTemplate(), executionContext);

		String notificationSubject = kaleoNotification.getDescription();

		String[] notificationTypes = StringUtil.split(
			kaleoNotification.getNotificationTypes());

		List<KaleoNotificationRecipient> kaleoNotificationRecipient =
			KaleoNotificationRecipientLocalServiceUtil.
				getKaleoNotificationRecipients(
					kaleoNotification.getKaleoNotificationId());

		if (kaleoNotificationRecipient.isEmpty()) {
			if (_log.isInfoEnabled()) {
				_log.info(
					"No recipients found to notify with message " +
					kaleoNotification.getName() + " " +
					notificationMessage);
			}

			return;
		}

		for (String notificationType : notificationTypes) {
			NotificationSender notificationSender =
				NotificationSenderFactory.getNotificationSender(
					notificationType);

			notificationSender.sendNotification(
				kaleoNotificationRecipient, notificationSubject,
				notificationMessage, executionContext);
		}
	}

	private static final Log _log = LogFactoryUtil.getLog(
		NotificationUtil.class);

}
