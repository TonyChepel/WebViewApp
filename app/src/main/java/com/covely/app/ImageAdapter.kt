package com.covely.app

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.covely.app.databinding.ImageItemBinding
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.util.*

class ImageAdapter(val imagelist : List<ImageData>,val activity : AppCompatActivity) : RecyclerView.Adapter<ImageAdapter.ImageHolder>() {
    class ImageHolder(val binding : ImageItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setData(item : ImageData, activity: AppCompatActivity) = with(binding){
         Picasso.get().load(item.image).centerCrop().resize(720,1280)
             .into(imMain,object :Callback{
                 override fun onSuccess() {
                     pbBar.visibility = View.GONE
                 }

                 override fun onError(e: Exception?) {

                 }

             })


            buttonSave.setOnClickListener {
                if(ContextCompat.checkSelfPermission(activity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),100)
                } else {
                    val externalStoreState = Environment.getExternalStorageState()
                    if(externalStoreState.equals(Environment.MEDIA_MOUNTED)){
                        try{
                            var storeDirectory = Environment.getExternalStorageDirectory().absolutePath
                            val file = File(storeDirectory,"${UUID.randomUUID()}.jpg")
                            val stream : OutputStream = FileOutputStream(file)
                            val drawable  = ContextCompat.getDrawable(activity.applicationContext,item.image)
                            val bitmap = (drawable as BitmapDrawable).bitmap
                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
                            stream.flush()
                            stream.close()

                            val snackBar = Snackbar.make((activity as MainActivity).findViewById(android.R.id.content),"Image is saving...",Snackbar.LENGTH_LONG)
                            snackBar.show()
                        }catch(e : Exception){
                            Toast.makeText(activity.applicationContext, "Error occured",Toast.LENGTH_LONG).show()
                        }


                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val binding = ImageItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ImageHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(imagelist[position],activity)

        if(holder.adapterPosition == 2) {
            holder.binding.arrowRight.visibility = View.GONE
            holder.binding.arrowLeft.visibility = View.VISIBLE

        } else if(holder.adapterPosition == 0){
            holder.binding.arrowRight.visibility = View.VISIBLE
            holder.binding.arrowLeft.visibility = View.GONE
        }


    }

    override fun getItemCount(): Int = imagelist.size


}