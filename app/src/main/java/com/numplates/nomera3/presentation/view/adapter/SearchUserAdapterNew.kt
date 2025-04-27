package com.numplates.nomera3.presentation.view.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.visible
import com.meera.core.utils.getAge
import com.meera.db.models.userprofile.VehicleCountry
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.UserSimple
import com.meera.db.models.userprofile.VehicleEntity
import com.meera.db.models.userprofile.VehicleType
import com.numplates.nomera3.presentation.view.widgets.VipView
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import timber.log.Timber

class SearchUserAdapterNew(private val act: Act) : RecyclerView.Adapter<SearchUserAdapterNew.ViewHolder>() {

    var collection = mutableListOf<UserSimple>()

    fun addItems(items: List<UserSimple>, isClearBeforeSet: Boolean = true) {
        if (isClearBeforeSet) {
            collection.clear()
        }
        collection.addAll(items)
        notifyDataSetChanged()
    }

    fun clearItems() {
        collection.clear()
        notifyDataSetChanged()
    }

    internal var clickListener: (UserSimple) -> Unit = { _ -> }

    override fun getItemCount(): Int {
        return collection.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_user_search_new))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(act, collection[position], clickListener)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val vipView: VipView = itemView.findViewById(R.id.vipView)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvBirthLocation: TextView = itemView.findViewById(R.id.tvBirthLocation)
        private val numberView: NumberPlateEditView = itemView.findViewById(R.id.nv_number_plate)
        private val ivBrandLogo: ImageView = itemView.findViewById(R.id.iv_logo_brand)
        private val cbBrandContainer: CardView = itemView.findViewById(R.id.cv_logo_container)
        private val uniqueNameTextView: TextView = itemView.findViewById(R.id.uniqueNameTextView)

        fun bind(act: Act, user: UserSimple, clickListener: (UserSimple) -> Unit) {
            Timber.d("SearchUser bind called with params ${user}")
            bind(act, user)

            itemView.setOnClickListener { clickListener(user) }
        }

        private fun bind(act: Act, user: UserSimple) {
            cbBrandContainer.gone()
            user.let { searchByNameModel ->
                vipView.setUp(
                        act,
                        searchByNameModel.avatarSmall,
                        searchByNameModel.accountType,
                        searchByNameModel.accountColor)

                tvName.text = searchByNameModel.name

                // отобразить уникальное имя @id12345678, если пустое, то скрыть
                searchByNameModel.uniqueName?.let { nonNullUniqueName: String ->
                    val formattedUniqueName = "@$nonNullUniqueName"
                    uniqueNameTextView.text = formattedUniqueName
                    uniqueNameTextView.visible()
                } ?: kotlin.run {
                    uniqueNameTextView.gone()
                }

                var age = ""
                searchByNameModel.birthday?.let { birth ->
                    age = getAge(birth)
                }

                if (age.isEmpty())
                    tvBirthLocation.text = searchByNameModel.city?.name?: ""
                else {
                    if (!searchByNameModel.city?.name.isNullOrEmpty())
                        tvBirthLocation.text = "$age, ${searchByNameModel.city?.name}"
                    else tvBirthLocation.text = age
                }

                searchByNameModel.mainVehicle?.let {
                    val vehicle = VehicleEntity(
                            it.number ?: "",
                            VehicleType(it.type?.typeId ?: -1),
                            VehicleCountry(it.country?.countryId?: -1)
                    )

                    setupNumberPlate(numberView, vehicle)

                    if (it.type?.typeId == 1) {
                        if (it.brandIcon != null) {
                            ivBrandLogo.loadGlide(it.brandIcon)
                            cbBrandContainer.visible()
                        } else {
                            cbBrandContainer.gone()
                        }

                        setupSizesTypeAvto()
                    } else if (it.type?.typeId == 2) {
                        numberView.visible()
                        setupSizesTypeMoto()
                    }
                } ?: kotlin.run {
                    numberView.gone()
                    cbBrandContainer.gone()
                }
            }
        }

        private fun setupSizesTypeAvto() {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)

            params.setMargins(
                    dpToPx(-91),
                    dpToPx(-16),
                    dpToPx(-91),
                    dpToPx(-16)
            )
            numberView.layoutParams = params
            numberView.scaleX = 0.4f
            numberView.scaleY = 0.4f
            numberView.visible()
        }

        private fun setupSizesTypeMoto() {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)

            params.setMargins(
                    dpToPx(-47),
                    dpToPx(-26),
                    dpToPx(-47),
                    dpToPx(-26)
            )
            numberView.layoutParams = params
            numberView.scaleX = 0.5f
            numberView.scaleY = 0.5f
            numberView.visible()
        }

        private fun setupNumberPlate(numberView: NumberPlateEditView, vehicle: VehicleEntity) {
            NumberPlateEditView.Builder(numberView)
                    .setVehicleNew(vehicle.number, vehicle.country?.countryId, vehicle.type?.typeId)
                    .build()
        }

    }

}
