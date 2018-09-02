/*
 * Copyright 2018, Jared Shane Stone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.util;

import android.content.Context;

import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder.OrderType;

/**
 * TODO: Add a class header comment!
 */
public class OrderUtils {

    private static final int ORDER_CATEGORY_KEYCHAINS = 0;
    private static final int ORDER_CATEGORY_TAFFY = 1;

    public static String[] getBillToLocations( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils
                        .getStringArrayResource( c, R.array.excel_cell_locations_bill_to_keychains );
            case ORDER_CATEGORY_TAFFY:
                return new String[]{};
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String[] getStoreNameLocations( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils
                        .getStringArrayResource( c, R.array.excel_cell_locations_store_number_keychains );
            case ORDER_CATEGORY_TAFFY:
                return StringUtils
                        .getStringArrayResource( c, R.array.excel_cell_locations_store_number_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String[] getRepNameLocations( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils
                        .getStringArrayResource( c, R.array.excel_cell_locations_rep_name_keychains );
            case ORDER_CATEGORY_TAFFY:
                return StringUtils.getStringArrayResource( c, R.array.excel_cell_locations_rep_name_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String[] getOrderDateLocations( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils
                        .getStringArrayResource( c, R.array.excel_cell_locations_order_date_keychains );
            case ORDER_CATEGORY_TAFFY:
                return StringUtils.getStringArrayResource( c, R.array.excel_cell_locations_order_date_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String[] getItemNameLocations( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils
                        .getStringArrayResource( c, R.array.excel_cell_locations_item_names_keychains );
            case ORDER_CATEGORY_TAFFY:
                return StringUtils.getStringArrayResource( c, R.array.excel_cell_locations_item_names_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String[] getItemQuantityLocations( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils
                        .getStringArrayResource( c, R.array.excel_cell_locations_item_quantities_keychains );
            case ORDER_CATEGORY_TAFFY:
                return StringUtils
                        .getStringArrayResource( c, R.array.excel_cell_locations_item_quantities_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String getFilename( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils.getStringResource( c, R.string.string_format_filename_keychains );
            case ORDER_CATEGORY_TAFFY:
                return StringUtils.getStringResource( c, R.string.string_format_filename_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String getFilenameForTemplate( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils.getStringResource( c, R.string.excel_template_filename_keychains );
            case ORDER_CATEGORY_TAFFY:
                return StringUtils.getStringResource( c, R.string.excel_template_filename_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String getSendToEmail( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                if ( PrefUtils.isCompanyDivisionDefault( c ) ) {
                    return StringUtils
                            .getStringResource( c, R.string.intent_extra_email_default_value_keychains );
                } else {
                    return StringUtils
                            .getStringResource( c, R.string.intent_extra_email_default_value_keychains_pugs );
                }
            case ORDER_CATEGORY_TAFFY:
                return StringUtils.getStringResource( c, R.string.intent_extra_email_default_value_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String getEmailSubject( Context c, int orderCategory, OrderType orderType ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                switch ( orderType ) {
                    case ORDER:
                        return StringUtils
                                .getStringResource( c, R.string.intent_extra_subject_send_order_by_email_keychains );
                    case ACKNOWLEDGEMENT:
                    case ACKNOWLEDGEMENT_WITH_ORDER:
                        return StringUtils
                                .getStringResource( c,
                                        R.string.intent_extra_subject_send_order_acknowledgement_by_email_keychains );
                    default:
                        throw new RuntimeException( "Invalid OrderType: " + orderType );
                }
            case ORDER_CATEGORY_TAFFY:
                switch ( orderType ) {
                    case ORDER:
                        return StringUtils
                                .getStringResource( c, R.string.intent_extra_subject_send_order_by_email_taffy );
                    case ACKNOWLEDGEMENT:
                    case ACKNOWLEDGEMENT_WITH_ORDER:
                        return StringUtils
                                .getStringResource( c,
                                        R.string.intent_extra_subject_send_order_acknowledgement_by_email_taffy );
                    default:
                        throw new RuntimeException( "Invalid OrderType: " + orderType );
                }
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String getEmailBody( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils
                        .getStringResource( c, R.string.intent_extra_text_body_send_order_by_email_keychains );
            case ORDER_CATEGORY_TAFFY:
                return StringUtils
                        .getStringResource( c, R.string.intent_extra_text_body_send_order_by_email_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static int[] getItemQuantities( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                if ( PrefUtils.isCompanyDivisionDefault( c ) ) {
                    return StringUtils
                            .getIntegerArrayResource( c, R.array.order_item_quantity_values_keychains );
                } else {
                    return StringUtils
                            .getIntegerArrayResource( c, R.array.order_item_quantity_values_keychains_pugs );
                }
            case ORDER_CATEGORY_TAFFY:
                return StringUtils.getIntegerArrayResource( c, R.array.order_item_quantity_values_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static int getOrderQuantityMinimum( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                if ( PrefUtils.isCompanyDivisionDefault( c ) ) {
                    return StringUtils
                            .getIntegerResource( c, R.integer.order_quantity_minimum_requirement_keychains );
                } else {
                    return StringUtils
                            .getIntegerResource( c, R.integer.order_quantity_minimum_requirement_keychains_pugs );
                }
            case ORDER_CATEGORY_TAFFY:
                return StringUtils
                        .getIntegerResource( c, R.integer.order_quantity_minimum_requirement_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String[] getBillToDetails( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                if ( PrefUtils.isCompanyDivisionDefault( c ) ) {
                    return StringUtils
                            .getStringArrayResource( c, R.array.excel_bill_to_values_keychains );
                } else {
                    return StringUtils
                            .getStringArrayResource( c, R.array.excel_bill_to_values_keychains_pugs );
                }
            case ORDER_CATEGORY_TAFFY:
                return new String[]{};
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

    public static String[] getBestSellers( Context c, int orderCategory ) {
        switch ( orderCategory ) {
            case ORDER_CATEGORY_KEYCHAINS:
                return StringUtils
                        .getStringArrayResource( c, R.array.best_sellers_keychains );
            case ORDER_CATEGORY_TAFFY:
                return StringUtils
                        .getStringArrayResource( c, R.array.best_sellers_taffy );
            default:
                throw new RuntimeException( "Invalid OrderCategory: " + orderCategory );
        }
    }

}
