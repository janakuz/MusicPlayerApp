package com.example.musicapp.data.repository

interface WorkerManagerRepository {

    fun startWorker(manual: Boolean = false)
}