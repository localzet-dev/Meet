/**
 * @package     Localzet Meet
 * @link        https://meet.localzet.com
 * @link        https://github.com/localzet-dev/Meet
 *
 * @author      Ivan Zorin <creator@localzet.com>
 * @copyright   Copyright (c) 2018-2024 Zorin Projects S.P.
 * @license     https://www.gnu.org/licenses/agpl-3.0 GNU Affero General Public License v3.0
 *
 *              This program is free software: you can redistribute it and/or modify
 *              it under the terms of the GNU Affero General Public License as published
 *              by the Free Software Foundation, either version 3 of the License, or
 *              (at your option) any later version.
 *
 *              This program is distributed in the hope that it will be useful,
 *              but WITHOUT ANY WARRANTY; without even the implied warranty of
 *              MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *              GNU Affero General Public License for more details.
 *
 *              You should have received a copy of the GNU Affero General Public License
 *              along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *              For any questions, please contact <creator@localzet.com>
 */

package com.localzet.meet.adapteritem

import com.localzet.meet.R
import com.localzet.meet.databinding.ItemMeetingHistoryBinding
import com.localzet.meet.model.Meeting
import android.view.LayoutInflater
import android.view.ViewGroup
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.mikepenz.fastadapter.binding.AbstractBindingItem

class MeetingHistoryItem(val meeting: Meeting) :
    AbstractBindingItem<ItemMeetingHistoryBinding>() {

    override val type: Int
        get() = R.id.fastadapter_meeting_history_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ItemMeetingHistoryBinding {
        return ItemMeetingHistoryBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: ItemMeetingHistoryBinding, payloads: List<Any>) {
        with(binding) {
            tvMeetingCode.text = meeting.code
            tvMeetingTime.text = root.context.getString(
                R.string.meeting_history_last_joined_time,
                TimeAgo.using(meeting.timeInMillis)
            )
        }

    }

    override fun unbindView(binding: ItemMeetingHistoryBinding) {
        with(binding) {
            tvMeetingCode.text = null
            tvMeetingTime.text = null
        }
    }


}