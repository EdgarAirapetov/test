package com.meera.core.extensions


/**
 * Вынес из коммон класса для теста краша с прода
 * Проверить начиная с 88 релиза
 *
 * Краш https://console.firebase.google.com/u/0/project/nomera-android/crashlytics/app/android:com.numplates.nomera3/issues/de8b551b512348d9d3175173e5e9ff6d?time=last-seven-days&types=crash&sessionEventKey=6620B62F003F00015B45E3BCBF44A6BB_1937555846405804081
 *
 * Caused by java.lang.NullPointerException: Parameter specified as non-null is null: method fg.b.i, parameter <this>
 *        at com.meera.core.extensions.CommonKt.i(Common.kt:3)
 *        at com.numplates.nomera3.presentation.view.ui.TextViewWithImages.setText(TextViewWithImages.kt:161)
 *        at android.widget.TextView.setText(TextView.java:6189)
 *        at android.widget.TextView.setMovementMethod(TextView.java:2607)
 *        at android.widget.TextView.setTextIsSelectable(TextView.java:7890)
 *        at android.widget.TextView.<init>(TextView.java:1409)
 *        at android.widget.TextView.<init>(TextView.java:1026)
 *        at androidx.appcompat.widget.AppCompatTextView.<init>(AppCompatTextView.java:113)
 *        at androidx.appcompat.widget.AppCompatTextView.<init>(AppCompatTextView.java:108)
 *        at com.numplates.nomera3.presentation.view.ui.TextViewWithImages.<init>(TextViewWithImages.kt:25)
 *        at java.lang.reflect.Constructor.newInstance0(Constructor.java)
 *
 * */
val <T : Any> T.simpleName: String
    get() = this::class.java.canonicalName ?: this::class.java.name
