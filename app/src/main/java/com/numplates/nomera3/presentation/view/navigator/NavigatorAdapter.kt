package com.numplates.nomera3.presentation.view.navigator

import android.os.Parcelable
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.meera.core.base.BaseFragment
import timber.log.Timber

class NavigatorAdapter(val fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager) {

    private val listOfFragments: MutableList<BaseFragment> = mutableListOf()

    lateinit var container: ViewGroup

    private var uniqueNumber = 0

    //Добавляем фрагмент
    fun addFragment(fragment: BaseFragment) {
        try {
            listOfFragments.add(fragment)
            notifyDataSetChanged()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    // Удаляем фрагмент
    fun removeLastFragment() {
        listOfFragments.removeAt(listOfFragments.size - 1)
        notifyDataSetChanged()
    }

    // Получаем размер стэка фрагментов
    fun getFragmentsCount(): Int {
        return listOfFragments.size
    }

    fun getCurrentFragment(): BaseFragment? {
        return listOfFragments.lastOrNull()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong() + uniqueNumber++
    }

    // Очищаем стек
    fun removeAllFragments() {
        fragmentManager.fragments.forEach { fragment ->
            if (fragment != null) {
                //Fatal Exception: java.lang.IllegalStateException
                //Can not perform this action after onSaveInstanceState
                // when used commitNow()
                fragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
//                fragmentManager.executePendingTransactions()
            }
        }
        listOfFragments.clear()
        notifyDataSetChanged()
    }

    // replace фрагмента
    fun replaceFragment(position: Int, fragment: BaseFragment) {
        listOfFragments[position] = fragment
        notifyDataSetChanged()
    }

    // получить стек фрагментов
    fun getListOfFragments(): MutableList<BaseFragment> {
        return listOfFragments
    }

    // удалить фрагмент из стека по порядковому номеру
    fun removeFragmentByIndex(fragmentPosition: Int) {
        listOfFragments.removeAt(fragmentPosition)
        notifyDataSetChanged()
    }

    fun returnToMainFragment() {
        while (listOfFragments.size > 1) {
            listOfFragments.removeAt(listOfFragments.size - 1)
            notifyDataSetChanged()
        }
    }


    override fun getItemPosition(`object`: Any): Int {
        val index = listOfFragments.indexOf(`object`)
//        Timber.e("INDEX_OF_FRAGMENT $index")
//        Timber.e("NAME_OF_FRAGMENT $`object`")

        // Используем для предотвращения дублирования фрагментов в стеке
        return if (index == -1)
            PagerAdapter.POSITION_NONE
        else
            index
    }

    override fun getItem(position: Int): Fragment {
        return listOfFragments[position]
    }

    override fun getCount(): Int {
        return listOfFragments.size
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        try {
            super.destroyItem(container, position, `object`)

            (`object` as? Fragment)?.let { fragment ->
                fragmentManager.beginTransaction().remove(fragment).commitNow()
            }
        } catch (e: Exception) {
            Timber.e("Cant destroy fragment: ${`object` as BaseFragment}. $e")
        }
    }
}
