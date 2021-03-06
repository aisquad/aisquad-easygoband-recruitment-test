package easygoband

import khttp.get
import org.json.JSONArray

class GetData() {
    // Variables to get data from known url.
    private val provider: Providers = Providers()
    private val url: String = provider.getApiURL()
    private var defaultEventID: Int = provider.getApiEventID()
    private val APIkey: String = provider.getApiKey()
    private val headers: Map<String, String> = mapOf(
        "content-type" to "application/json",
        "Authorization" to "Basic $APIkey"
    )
    private var shown = 0
    private var mistyping = 0
    private var mistypingList = mutableListOf<String>()

    // Attributes
    private val nameSet: MutableSet<String> = mutableSetOf()
    val elements: MutableList<Element> = mutableListOf()

    // Methods
    private fun getURL(eventID: Int?): JSONArray {
        val response = get("$url${eventID ?: defaultEventID}", headers = headers)
        return response.jsonArray
    }

    fun getNames(): List<String> {
        return nameSet.sorted()
    }

    fun showSessionNames() {
        if (nameSet.size == 0) {
            println("No data were loaded.")
            return
        }
        for (key in getNames())
            println(key)
        println("\nThere are ${elements.size} elements for ${nameSet.size} different session names.")
    }

    fun run() {
        val data = getURL(null)
        elements.addAll(ElementHandler(data).handle())
        val list = ElementHandler(data)
        list.check()
        elements.forEach {
            nameSet.add(it.getSessionNames())
        }
    }

    fun showDataFromSessionName(sessionName: String){
        if (!nameSet.contains(sessionName)) {
            mistypingList.add(sessionName)
        } else {
            for (element in elements)
                if (element.hasSessionName(sessionName)) {
                    println("${++shown}.-")
                    element.show()
                }
        }
    }

    fun handleSessionNames(args: Array<String>){
            if (args.size > 1) {
                for (i in 1 until args.size) {
                    showDataFromSessionName(args[i])
                }
                if (mistypingList.isNotEmpty()) {
                    println("Unkown session names: ${mistypingList.joinToString(", ")}")
                    println("Session name must be one of the following names:")
                    showSessionNames()
                }
            } else {
                println("A filter value is required, please choose one of this values:")
                getNames().forEach{println(it)}
            }
    }

    fun showData() {
        var shown = 0
        for (element in elements) {
            println("${++shown}.- ")
            element.show()
        }
    }
}

fun main(args: Array<String>) {
    val data = GetData()
    data.run()
    val errMessage = "Please, choose one of the following options:\n\t--names\n\t--filter <session name>\n\t--all"
    if (args.isNotEmpty()) {
        when (args[0]) {
            "--all" -> data.showData()
            "--filter" -> data.handleSessionNames(args)
            "--names" -> data.showSessionNames()
            else -> println(errMessage)
        }
    } else
        println(errMessage)
}

