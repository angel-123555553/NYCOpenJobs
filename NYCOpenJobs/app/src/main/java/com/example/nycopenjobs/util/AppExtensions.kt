package com.example.nycopenjobs.util

/*
* extension function to provide TAG value
 */

val Any.TAG: String
    get() {

        return if (!javaClass.isAnonymousClass) {
            val name = javaClass.simpleName

            //first 23 characters
            if (name.length <= 23) name else name.substring(startIndex = 0, 23)

        } else{
            val name = javaClass.name
            if (name.length <= 23) name else name.substring(name.length - 23, name.length)
        }
    }