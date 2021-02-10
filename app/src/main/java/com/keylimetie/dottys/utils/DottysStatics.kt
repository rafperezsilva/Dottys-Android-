package com.keylimetie.dottys.utils

import com.keylimetie.dottys.ui.drawing.models.DottysDrawing


 class DottysStatics {
     companion object {
         val PICTURE_TAKE_REQUEST_CODE = 9999
         val staticCashRewards = arrayListOf(
             DottysDrawing(
                 null, null, null, null, null,
                 "$10\n Cash Reward", "1,000 Points to $10", null,
                 null, null, null, null, null,
                 null, null, null, null),
             DottysDrawing(
                 null, null, null, null, null,
                 "$20\n Cash Reward", "2,000 Points to $20", null,
                 null, null, null, null, null,
                 null, null, null, null),
             DottysDrawing(
                 null, null, null, null, null,
                 "$50\n Cash Reward", "5,000 Points to $50", null,
                 null, null, null, null, null,
                 null, null, null, null),

             )
     }
}