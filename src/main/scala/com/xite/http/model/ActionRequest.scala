package com.xite.http.model

import com.xite.model.{ActionId, UserId, VideoId}

final case class ActionRequest(userId: UserId, videoId: VideoId, actionId: ActionId)
