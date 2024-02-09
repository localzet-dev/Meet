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

package com.localzet.meet.db

import com.localzet.meet.model.Meeting
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Dao to save and access the meetings done by the user
 */
@Dao
interface MeetingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: Meeting): Long

    @Query("SELECT * FROM meetings ORDER BY timeInMillis DESC")
    fun getAllMeetings(): LiveData<List<Meeting>>

    @Query("DELETE FROM meetings where  code in (:meetingCodeList)")
    suspend fun deleteMultipleMeetings(meetingCodeList: List<String>)
}