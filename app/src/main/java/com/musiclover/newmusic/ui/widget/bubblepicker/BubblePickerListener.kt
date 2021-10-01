package com.musiclover.newmusic.ui.widget.bubblepicker

import com.musiclover.newmusic.ui.widget.bubblepicker.model.PickerItem

/**
 * Created by irinagalata on 3/6/17.
 */
interface BubblePickerListener {

    fun onBubbleSelected(item: PickerItem, position: Int)

    fun onBubbleDeselected(item: PickerItem, position: Int)

}