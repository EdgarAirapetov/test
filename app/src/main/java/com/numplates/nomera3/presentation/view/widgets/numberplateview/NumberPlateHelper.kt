package com.numplates.nomera3.presentation.view.widgets.numberplateview

import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues


object NumberPlateHelper {

    fun setBackground(vehicleType: String?, vehicleCountry: String?, accountType: Int?, accountColor: Int?): Int {
        return when (vehicleType) {
            "1" -> {
                when (accountType) {
                    INetworkValues.ACCOUNT_TYPE_REGULAR -> {
                        when (vehicleCountry) {
                            //россия
                            INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                R.drawable.number_default_ru_auto
                            }
                            //украина
                            INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                R.drawable.number_default_ua_auto
                            }
                            //беларусь
                            INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                R.drawable.number_default_by_auto
                            }
                            //грузия
                            INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                R.drawable.number_default_ge_auto
                            }
                            //Казахстан
                            INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                R.drawable.number_default_kz_auto
                            }
                            //армения
                            INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                R.drawable.number_default_am_auto
                            }

                            else -> 0
                        }
                    }

                    INetworkValues.ACCOUNT_TYPE_PREMIUM -> {
                        when (accountColor) {
                            INetworkValues.COLOR_RED -> {
                                when (vehicleCountry) {
                                    //россия
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_red_ru_auto
                                    }
                                    //украина
                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_red_ua_auto
                                    }
                                    //беларусь
                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_red_by_auto
                                    }
                                    //грузия
                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_red_ge_auto
                                    }
                                    //Казахстан
                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_red_kz_auto
                                    }
                                    //армения
                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_red_am_auto
                                    }

                                    else -> 0
                                }
                            }

                            INetworkValues.COLOR_GREEN -> {
                                when (vehicleCountry) {
                                    //россия
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_green_ru_auto
                                    }
                                    //украина
                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_green_ua_auto
                                    }
                                    //беларусь
                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_green_by_auto
                                    }
                                    //грузия
                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_green_ge_auto
                                    }
                                    //Казахстан
                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_green_kz_auto
                                    }
                                    //армения
                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_green_am_auto
                                    }

                                    else -> 0
                                }
                            }

                            INetworkValues.COLOR_BLUE -> {
                                when (vehicleCountry) {
                                    //россия
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_blue_ru_auto
                                    }
                                    //украина
                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_blue_ua_auto
                                    }
                                    //беларусь
                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_blue_by_auto
                                    }
                                    //грузия
                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_blue_ge_auto
                                    }
                                    //Казахстан
                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_blue_kz_auto
                                    }
                                    //армения
                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_blue_am_auto
                                    }

                                    else -> 0
                                }
                            }

                            INetworkValues.COLOR_PINK -> {
                                when (vehicleCountry) {
                                    //россия
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_pink_ru_auto
                                    }
                                    //украина
                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_pink_ua_auto
                                    }
                                    //беларусь
                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_pink_by_auto
                                    }
                                    //грузия
                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_pink_ge_auto
                                    }
                                    //Казахстан
                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_pink_kz_auto
                                    }
                                    //армения
                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_pink_am_auto
                                    }

                                    else -> 0
                                }
                            }

                            INetworkValues.COLOR_PURPLE -> {
                                when (vehicleCountry) {
                                    //россия
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_purple_ru_auto
                                    }
                                    //украина
                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_purple_ua_auto
                                    }
                                    //беларусь
                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_purple_by_auto
                                    }
                                    //грузия
                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_purple_ge_auto
                                    }
                                    //Казахстан
                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_purple_kz_auto
                                    }
                                    //армения
                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_purple_am_auto
                                    }

                                    else -> 0
                                }
                            }

                            else -> 0
                        }
                    }

                    INetworkValues.ACCOUNT_TYPE_VIP -> {
                        when (vehicleCountry) {
                            //россия
                            INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                R.drawable.number_vip_ru_auto_vip
                            }
                            //украина
                            INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                R.drawable.number_vip_ua_auto_vip
                            }
                            //беларусь
                            INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                R.drawable.number_vip_by_auto_vip
                            }
                            //грузия
                            INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                R.drawable.number_vip_ge_auto_vip
                            }
                            //Казахстан
                            INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                R.drawable.number_vip_kz_auto_vip
                            }
                            //армения
                            INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                R.drawable.number_vip_am_auto_vip
                            }

                            else -> 0
                        }
                    }

                    else -> 0
                }
            }

            "2" -> {
                when (accountType) {
                    INetworkValues.ACCOUNT_TYPE_REGULAR -> {
                        when (vehicleCountry) {
                            //россия
                            INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                R.drawable.number_default_ru_moto
                            }
                            //украина
                            INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                R.drawable.number_default_ua_moto
                            }
                            //беларусь
                            INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                R.drawable.number_default_by_moto
                            }
                            //грузия
                            INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                R.drawable.number_default_ge_moto
                            }
                            //Казахстан
                            INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                R.drawable.number_kz_moto
                            }
                            //армения
                            INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                R.drawable.number_default_am_moto
                            }

                            else -> 0
                        }
                    }

                    INetworkValues.ACCOUNT_TYPE_PREMIUM -> {
                        when (accountColor) {
                            INetworkValues.COLOR_RED -> {
                                when (vehicleCountry) {
                                    //россия
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_red_ru_moto
                                    }
                                    //украина
                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_red_ua_moto
                                    }
                                    //беларусь
                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_red_by_moto
                                    }
                                    //грузия
                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_red_ge_moto
                                    }
                                    //Казахстан
                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_red_kz_moto
                                    }
                                    //армения
                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_red_am_moto
                                    }

                                    else -> 0
                                }
                            }

                            INetworkValues.COLOR_GREEN -> {
                                when (vehicleCountry) {
                                    //россия
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_green_ru_moto
                                    }
                                    //украина
                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_green_ua_moto
                                    }
                                    //беларусь
                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_green_by_moto
                                    }
                                    //грузия
                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_green_ge_moto
                                    }
                                    //Казахстан
                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_green_kz_moto
                                    }
                                    //армения
                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_green_am_moto
                                    }

                                    else -> 0
                                }
                            }

                            INetworkValues.COLOR_BLUE -> {
                                when (vehicleCountry) {
                                    //россия
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_blue_ru_moto
                                    }
                                    //украина
                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_blue_ua_moto
                                    }
                                    //беларусь
                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_blue_by_moto
                                    }
                                    //грузия
                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_blue_ge_moto
                                    }
                                    //Казахстан
                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_blue_kz_moto
                                    }
                                    //армения
                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_blue_am_moto
                                    }

                                    else -> 0
                                }
                            }

                            INetworkValues.COLOR_PINK -> {
                                when (vehicleCountry) {
                                    //россия
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_pink_ru_moto
                                    }
                                    //украина
                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_pink_ua_moto
                                    }
                                    //беларусь
                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_pink_by_moto
                                    }
                                    //грузия
                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_pink_ge_moto
                                    }
                                    //Казахстан
                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_pink_kz_moto
                                    }
                                    //армения
                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_pink_am_moto
                                    }

                                    else -> 0
                                }
                            }

                            INetworkValues.COLOR_PURPLE -> {
                                when (vehicleCountry) {
                                    INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                        R.drawable.number_purple_ru_moto
                                    }

                                    INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                        R.drawable.number_purple_ua_moto
                                    }

                                    INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                        R.drawable.number_purple_by_moto
                                    }

                                    INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                        R.drawable.number_purple_ge_moto
                                    }

                                    INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                        R.drawable.number_purple_kz_moto
                                    }

                                    INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                        R.drawable.number_purple_am_moto
                                    }

                                    else -> 0
                                }
                            }

                            else -> 0
                        }
                    }

                    INetworkValues.ACCOUNT_TYPE_VIP -> {
                        when (vehicleCountry) {
                            //россия
                            INetworkValues.ROAD_TO_RUSSIA.toString() -> {
                                R.drawable.number_vip_ru_moto_vip
                            }
                            //украина
                            INetworkValues.ROAD_TO_UKRAINE.toString() -> {
                                R.drawable.number_vip_ua_moto_vip
                            }
                            //беларусь
                            INetworkValues.ROAD_TO_BELORUS.toString() -> {
                                R.drawable.number_vip_by_moto_vip
                            }
                            //грузия
                            INetworkValues.ROAD_TO_GEORGIA.toString() -> {
                                R.drawable.number_vip_ge_moto_vip
                            }
                            //Казахстан
                            INetworkValues.ROAD_TO_KAZAKHSTAN.toString() -> {
                                R.drawable.number_vip_kz_moto_vip
                            }
                            //армения
                            INetworkValues.ROAD_TO_ARMENIA.toString() -> {
                                R.drawable.number_vip_am_moto_vip
                            }

                            else -> 0
                        }
                    }

                    else -> 0
                }
            }

            else -> {
                0
            }
        }
    }
}
