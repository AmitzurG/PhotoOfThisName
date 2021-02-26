package com.example.photosthisname.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photosthisname.R
import com.example.photosthisname.databinding.ActivityMainBinding
import com.example.photosthisname.viewmodel.PhotosViewModel


class MainActivity : AppCompatActivity() {
    companion object {
        private const val PERMISSIONS_REQUEST_READ_CONTACTS = 11
    }

    private val photosViewModel: PhotosViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var searchView: SearchView? = null

    private val activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(it.resultCode == Activity.RESULT_OK) {
            val uri = it?.data?.data
            if (uri != null) {
                val cursor = contentResolver.query(uri, null, null, null, null)
                if (cursor?.moveToFirst() == true) {
                    //val phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val splitName = name.split(" ")
                    val firstName = if (splitName.isNotEmpty()) splitName[0] else name
                    showPhotosList(firstName) // first name as a tag
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))
        setPhotosRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchMenuItem = menu.findItem(R.id.nameSearchView)
        searchView = searchMenuItem?.actionView as? SearchView
        setPhotosSearchView()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.actionPickContact -> {
                pickContact()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // permission is granted
                pickContact()
            }
        }
    }

    // region private functions
    private fun setPhotosRecyclerView() = binding.photosRecyclerView.apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        val dividerItemDecoration = DividerItemDecoration(context, RecyclerView.VERTICAL)
        addItemDecoration(dividerItemDecoration)
        adapter = PhotosRecyclerViewAdapter()
        (adapter as? PhotosRecyclerViewAdapter)?.loadMorePhotos = { // if arrive to the end of the photos list load more photos
            showProgressBar()
            photosViewModel.loadMorePhotos().observe(this@MainActivity, {
                (binding.photosRecyclerView.adapter as? PhotosRecyclerViewAdapter)?.photos = it.toMutableList()
                showProgressBar(false)
            })
        }
    }

    private fun setPhotosSearchView() {
        searchView?.apply {
            queryHint = getString(R.string.enterName)
        }
        setPhotosSearchViewListener()
    }

    private fun setPhotosSearchViewListener()  = searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(name: String): Boolean {
            searchView?.setQuery("", false)
            searchView?.isIconified = true
            showPhotosList(name)
            return true
        }

        override fun onQueryTextChange(searchText: String) = true
    })

    private fun pickContact() = if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), PERMISSIONS_REQUEST_READ_CONTACTS)
    } else {
        searchView?.setQuery("", false)
        searchView?.isIconified = true
        activityResultLauncher.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
    }

    private fun showPhotosList(tag: String) {
        showProgressBar()
        photosViewModel.getPhotos(tag).observe(this, {
            if (it.isNullOrEmpty()) {
                showNoFoundPhotos()
            } else {
                showNoFoundPhotos(false)
                (binding.photosRecyclerView.adapter as? PhotosRecyclerViewAdapter)?.photos = it.toMutableList()
            }
            showProgressBar(false)
        })
    }

    private fun showProgressBar(show: Boolean = true) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showNoFoundPhotos(show: Boolean = true) {
        binding.noPhotos.visibility = if (show) View.VISIBLE else View.GONE
    }
    // endregion
}