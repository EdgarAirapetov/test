package com.numplates.nomera3.presentation.view.view;

import com.numplates.nomera3.data.network.EmptyModel;
import com.numplates.nomera3.presentation.router.INetView;

/**
 * created by c7j on 18.06.18
 */
public interface ISendCodeView extends INetView {

    void onGetSendCodeOk(EmptyModel res);
    void onGetSendCodeFail(String msg);

}
