package com.remitty.caronz.helper;

import com.remitty.caronz.models.ReceivedMessageModel;

public interface SendReciveONClickListner {
    void onItemClick(ReceivedMessageModel item);
    void onItemDelete(ReceivedMessageModel item, int position);
}
