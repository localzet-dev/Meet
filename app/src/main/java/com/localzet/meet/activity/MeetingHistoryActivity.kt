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

package com.localzet.meet.activity

import com.core.extensions.copyTextToClipboard
import com.core.extensions.makeGone
import com.core.extensions.makeVisible
import com.localzet.meet.Meetly
import com.localzet.meet.R
import com.localzet.meet.adapteritem.MeetingHistoryItem
import com.localzet.meet.databinding.ActivityMeetingHistoryBinding
import com.localzet.meet.model.Meeting
import com.localzet.meet.utils.MeetingUtils
import com.localzet.meet.viewmodel.MeetingHistoryViewModel
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.item_meeting_history.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MeetingHistoryActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, MeetingHistoryActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityMeetingHistoryBinding
    private val viewModel by viewModel<MeetingHistoryViewModel>() // Lazy inject ViewModel

    private lateinit var meetingHistoryAdapter: FastItemAdapter<MeetingHistoryItem>
    private var initialLayoutComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeetingHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView(savedInstanceState)
        setupObservables()
    }

    override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        outState = meetingHistoryAdapter.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }


    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupRecyclerView(savedInstanceState: Bundle?) {
        meetingHistoryAdapter = FastItemAdapter()
        meetingHistoryAdapter.setHasStableIds(true)
        meetingHistoryAdapter.withSavedInstanceState(savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = meetingHistoryAdapter

        onMeetingCodeClick()
        onRejoinClick()
    }

    private fun setupObservables() {
        viewModel.meetingHistoryLiveData.observe(this, Observer { meetingHistoryList ->
            val meetingHistoryItems = ArrayList<MeetingHistoryItem>()

            for (meeting in meetingHistoryList) {
                meetingHistoryItems.add(MeetingHistoryItem(meeting))
            }

            FastAdapterDiffUtil[meetingHistoryAdapter.itemAdapter] = meetingHistoryItems
            showEmptyState(meetingHistoryAdapter.itemCount)
        })
    }

    private fun showEmptyState(itemCount: Int) {
        if (itemCount > 0) binding.groupEmpty.makeGone() else binding.groupEmpty.makeVisible()
    }


    /**
     * Called when the meeting code is clicked of a RecyclerView Item
     */
    private fun onMeetingCodeClick() {
        meetingHistoryAdapter.addEventHook(object : ClickEventHook<MeetingHistoryItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return viewHolder.itemView.tvMeetingCode
            }

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<MeetingHistoryItem>,
                item: MeetingHistoryItem
            ) {
                copyTextToClipboard(
                    item.meeting.code,
                    getString(R.string.meeting_history_meeting_code_copied)
                )
            }
        })
    }

    /**
     * Called when the Rejoin button is clicked of a RecyclerView Item
     */
    private fun onRejoinClick() {
        meetingHistoryAdapter.addEventHook(object : ClickEventHook<MeetingHistoryItem>() {
            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                return viewHolder.itemView.btnRejoinMeeting
            }

            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<MeetingHistoryItem>,
                item: MeetingHistoryItem
            ) {
                MeetingUtils.startMeeting(
                    this@MeetingHistoryActivity,
                    item.meeting.code,
                    R.string.all_rejoining_meeting
                ) // Start Meeting

                viewModel.addMeetingToDb(
                    Meeting(
                        item.meeting.code,
                        System.currentTimeMillis()
                    )
                ) // Add meeting to db
            }
        })
    }
}
