package moe.crx.effect.utils

fun generatePages(page: Long, pagesCount: Long): List<Long> {
    val pageList = mutableListOf<Long>()

    for (i in (page-3)..page) {
        if (i > 0) pageList.add(i)
    }
    for (i in (page + 1)..(page+3)) {
        if (i <= pagesCount) pageList.add(i)
    }

    return pageList
}

fun parseSearchQuery(query: String): Map<String, String> {
    val map = query
        .split(";")
        .associate {
            val split = it.indexOf('=')

            if (split != -1) {
                Pair(it.substring(0, split), it.substring(split + 1))
            } else {
                Pair(it, "")
            }
        }.toMutableMap()

    if (map.size == 1 && map.values.first().isBlank() && !query.isBlank()) {
        map["text"] = query
    }

    return map
}