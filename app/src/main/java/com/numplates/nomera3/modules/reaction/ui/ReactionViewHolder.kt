package com.numplates.nomera3.modules.reaction.ui

import com.numplates.nomera3.modules.reaction.data.ReactionUpdate

/**
 * Интерфейс для интеграции реакций в старые легаси-ячейки
 * (у старых легаси-ячеек нет единого класса, поэтому чтобы привести их к единому виду используется интерфейс)
 *
 * P.S. можно удалить когда отрефакторят адаптеры
 * PagePostListAdapter и прочее...
 */
interface ReactionViewHolder {
    fun bindReactionPayload(reactionUpdate: ReactionUpdate)
}