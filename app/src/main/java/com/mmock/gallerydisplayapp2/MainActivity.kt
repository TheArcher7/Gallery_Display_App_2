package com.mmock.gallerydisplayapp2

//Micah Mock
//Colorado State University Global
//CSC457 Platform-based Development
//Professor Chintan Thakkar
//12/21/2023

//Resources used
//https://youtu.be/iZYLarknOvg?si=zc3XXcMBxzLkMFHM

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

//variables for debugging
private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private var imageRecycler : RecyclerView? = null
    private var progressBar : ProgressBar? = null
    private var allPictures : ArrayList<Image>? = null

    private val MY_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageRecycler = findViewById(R.id.image_recycler)
        progressBar = findViewById(R.id.recycler_progress)

        imageRecycler?.layoutManager = GridLayoutManager(this, 3)
        imageRecycler?.setHasFixedSize(true)


        // start of FIXME (permission request not being displayed on screen)
        // The following are 3 blocks of code to request the same permission
        // but in 3 different ways according to what has been researched.

        // FIXME code 1
        // Permissions for storage access
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if(ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED){
            //ask for permission
            Log.d(TAG, "Storage permissions not granted. Requesting permissions. (code 1)")
            ActivityCompat.requestPermissions(this, arrayOf(permission) , MY_PERMISSION_CODE)
        } else {
            Log.d(TAG, "Storage permissions granted. (code 1)")
            Toast.makeText(this, "Accessing photos from gallery.", Toast.LENGTH_SHORT).show()
        }

        // FIXME code 2
        // This code is copied from the android documentation.
        // It asks for permissions in a similar way to the first, but doesn't work
        when {
            ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                //Code goes here
                Log.d(TAG, "Storage permissions granted. (code 2)")
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, permission) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
            }
            else -> {
                // You can directly ask for the permission.
                Log.d(TAG, "Storage permissions not granted. Requesting permissions. (code 2)")
                ActivityCompat.requestPermissions(this,
                    arrayOf(permission),
                    MY_PERMISSION_CODE)
            }
        }

        // FIXME code 3
        // This code is also taken from the android documentation
        // ActivityResultLauncher. You can use either a val, as shown in this snippet, or a lateinit var in your onAttach() or onCreate() method.
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your app.
                    Log.d(TAG, "Storage permissions granted. (code 3.1)")
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their decision.
                    Log.d(TAG, "Storage permissions not granted. Requesting permissions. (code 3.1)")
                }
            }
        //Additional code
        when {
            ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                Log.d(TAG, "Storage permissions granted. (code 3.2)")
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, permission) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                Log.d(TAG, "Storage permissions not granted. Requesting permissions. (code 3.2)")
                requestPermissionLauncher.launch(permission)
                //it is expected that a 3.2 request would come before a 3.1 because of how it is called
            }
        }
        // end of FIXME (permission request not being displayed on screen)


        allPictures = ArrayList()

        if(allPictures!!.isEmpty()){
            progressBar?.visibility = View.VISIBLE
            //get all images from storage
            allPictures = getAllImages()
            //set adapter for recyclerView
            imageRecycler?.adapter = ImageAdapter(this, allPictures!!)
            progressBar?.visibility = View.GONE
        }
    }

    private fun getAllImages(): ArrayList<Image>? {
        Log.d(TAG, "Getting images.")
        val images = ArrayList<Image>()
        val allImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA, MediaStore.Images.Media.DISPLAY_NAME)

        val cursor = this@MainActivity.contentResolver.query(allImageUri, projection, null, null, null)

        try {
            cursor!!.moveToFirst()
            do{
                val image = Image()
                image.imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                image.imageName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                images.add(image)
            }while(cursor.moveToNext())

            Log.d(TAG, "Image loading complete.")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "Error getting images.")
        } finally {
            cursor?.close()
        }
        return images
    }
}