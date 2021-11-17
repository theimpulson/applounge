/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.utils

import foundation.e.apps.R

sealed class Error {
    object NoError : Error()

    data class NetworkError(
        val drawable: Int = R.drawable.ic_network_unavailable,
        val title: Int = R.string.network_error_title,
        val desc: Int = R.string.network_error_desc
    ) : Error()

    data class InternalError(
        val drawable: Int = R.drawable.ic_broken_link,
        val title: Int = R.string.internal_error_title,
        val desc: Int = R.string.internal_error_desc
    ) : Error()

    data class NoResults(
        val drawable: Int = R.drawable.ic_404,
        val title: Int = R.string.no_results_title,
        val desc: Int = R.string.no_results_desc
    ) : Error()

    data class UnImplemented(
        val drawable: Int = R.drawable.ic_maintenance,
        val title: Int = R.string.unimplemented_error_title,
        val desc: Int = R.string.unimplemented_error_title
    ) : Error()
}
