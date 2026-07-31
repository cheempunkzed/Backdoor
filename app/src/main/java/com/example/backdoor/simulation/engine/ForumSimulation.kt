package com.example.backdoor.simulation.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

data class ForumPost(
    val id: String = UUID.randomUUID().toString(),
    val authorHandle: String,
    val content: String,
    val upvotes: Int = 0
)

data class ForumThread(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: String,
    val authorHandle: String,
    val posts: MutableList<ForumPost> = mutableListOf()
)

class ForumSimulation {
    private val _threads = MutableStateFlow<List<ForumThread>>(emptyList())
    val threads: StateFlow<List<ForumThread>> = _threads.asStateFlow()

    private val titles = listOf(
        "Anyone noticing higher latency on corporate nodes?",
        "Zero-day drop for Abyss OS router daemon",
        "Looking for freelance penetration testers",
        "Has anyone seen the market crash coming?",
        "New security protocol bypassed in 5 minutes",
        "Rumor: Major corp data leak happening tonight"
    )

    private val authors = listOf("shadow_broker", "anon_992", "sysadmin_bob", "netrunner_x", "ghost_in_shell")

    init {
        // Initial dummy data
        for (i in 0 until 3) {
            generateThread()
        }
    }

    fun tick() {
        if (Random.nextFloat() < 0.05f) {
            generateThread()
        }
        if (Random.nextFloat() < 0.1f && _threads.value.isNotEmpty()) {
            val thread = _threads.value.random()
            val newPosts = thread.posts.toMutableList()
            newPosts.add(ForumPost(authorHandle = authors.random(), content = "Interesting observation. I will look into it."))
            val updatedThread = thread.copy(posts = newPosts)
            _threads.value = _threads.value.map { if (it.id == thread.id) updatedThread else it }
        }
    }

    private fun generateThread() {
        val newThread = ForumThread(
            title = titles.random(),
            category = listOf("General", "Exploits", "Market").random(),
            authorHandle = authors.random()
        )
        newThread.posts.add(ForumPost(authorHandle = newThread.authorHandle, content = "Just posting this here for visibility. What do you guys think?"))
        _threads.value = (listOf(newThread) + _threads.value).take(50)
    }
    
    fun postReplyToThread(threadId: String, content: String, author: String) {
        val thread = _threads.value.find { it.id == threadId }
        if (thread != null) {
            val newPosts = thread.posts.toMutableList()
            newPosts.add(ForumPost(authorHandle = author, content = content))
            val updatedThread = thread.copy(posts = newPosts)
            _threads.value = _threads.value.map { if (it.id == threadId) updatedThread else it }
        }
    }

    fun serializeToJson(): String {
        val arr = JSONArray()
        for (t in _threads.value) {
            val tObj = JSONObject()
            tObj.put("id", t.id)
            tObj.put("title", t.title)
            tObj.put("category", t.category)
            tObj.put("authorHandle", t.authorHandle)
            val pArr = JSONArray()
            for (p in t.posts) {
                val pObj = JSONObject()
                pObj.put("id", p.id)
                pObj.put("authorHandle", p.authorHandle)
                pObj.put("content", p.content)
                pObj.put("upvotes", p.upvotes)
                pArr.put(pObj)
            }
            tObj.put("posts", pArr)
            arr.put(tObj)
        }
        return arr.toString()
    }

    fun deserializeFromJson(json: String) {
        try {
            val arr = JSONArray(json)
            val newThreads = mutableListOf<ForumThread>()
            for (i in 0 until arr.length()) {
                val tObj = arr.getJSONObject(i)
                val t = ForumThread(
                    id = tObj.getString("id"),
                    title = tObj.getString("title"),
                    category = tObj.getString("category"),
                    authorHandle = tObj.getString("authorHandle")
                )
                val pArr = tObj.getJSONArray("posts")
                for (j in 0 until pArr.length()) {
                    val pObj = pArr.getJSONObject(j)
                    t.posts.add(ForumPost(
                        id = pObj.getString("id"),
                        authorHandle = pObj.getString("authorHandle"),
                        content = pObj.getString("content"),
                        upvotes = pObj.getInt("upvotes")
                    ))
                }
                newThreads.add(t)
            }
            _threads.value = newThreads
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
