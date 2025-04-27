package com.numplates.nomera3.modules.search.domain.mapper.result

import com.numplates.nomera3.domain.util.Mapper
import com.numplates.nomera3.modules.search.ui.entity.SearchItem

class GroupSubscribeStatusButtonMapper: Mapper<Boolean, SearchItem.Group.ButtonState> {

    /**
     * @param entity - is subscribe community or not
     */
    override fun map(entity: Boolean): SearchItem.Group.ButtonState {
        return if(entity){
            SearchItem.Group.ButtonState.Hide
        } else {
            SearchItem.Group.ButtonState.Show
        }
    }
}