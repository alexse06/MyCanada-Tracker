package com.example.ircctracker.data.remote

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

/**
 * Parses the official IRCC RSS/Atom feed to extract news headlines.
 * Removes the need for AI to summarize news.
 */
class RssParser {

    fun parse(inputStream: InputStream): List<String> {
        val titles = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()

        return readFeed(parser)
    }

    private fun readFeed(parser: XmlPullParser): List<String> {
        val entries = mutableListOf<String>()
        
        // Atom feeds usually have <entry> tags, RSS has <item>
        // We'll look for both to be safe.
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name == "entry" || parser.name == "item") {
                    entries.add(readEntry(parser))
                }
            }
            eventType = parser.next()
        }
        return entries.take(5) // Return top 5 recent news
    }

    private fun readEntry(parser: XmlPullParser): String {
        var title = ""
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "title") {
                title = readTitle(parser)
            } else {
                skip(parser)
            }
        }
        return title
    }

    private fun readTitle(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        // Clean up common prefixes if needed, but usually raw title is fine
        return result.trim()
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}
