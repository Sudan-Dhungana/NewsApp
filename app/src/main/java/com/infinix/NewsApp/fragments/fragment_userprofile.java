package com.infinix.NewsApp.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.infinix.NewsApp.R;
import com.infinix.NewsApp.UserLoginRegister.UserLoginActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_userprofile#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_userprofile extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    String encodeImageString;
    TextView txtAddImage;
    com.makeramen.roundedimageview.RoundedImageView imageProfile;
    private ActivityResultLauncher<Intent> galleryLauncher;

    public fragment_userprofile() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_userprofile.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_userprofile newInstance(String param1, String param2) {
        fragment_userprofile fragment = new fragment_userprofile();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView profileName, profileEmail;
        View rootView = inflater.inflate(R.layout.fragment_userprofile, container, false);

        profileName = rootView.findViewById(R.id.profile_name);
        profileEmail = rootView.findViewById(R.id.profile_email);

        TextView projectFirstTV = rootView.findViewById(R.id.projectFirstTV);
        TextView projectSecondTV = rootView.findViewById(R.id.projectSecondTV);
        TextView projectSecondHomeTV = rootView.findViewById(R.id.projectSecondHomeTV);

        imageProfile = rootView.findViewById(R.id.imageProfile);
        txtAddImage = rootView.findViewById(R.id.txtAddImage);
        com.google.android.material.button.MaterialButton imgSetBtn =
                rootView.findViewById(R.id.idImgSet);

        if (imageProfile != null) {
            String userName = requireActivity().getIntent().getStringExtra("user_name");
            try {
                userName = URLEncoder.encode(userName, "UTF-8");
                String urlRetrieveImg = getString(R.string.ip_address).trim() +
                        "NewsApp/retrieveImage.php?username=" + userName;
                RequestQueue retrieveQueue = Volley.newRequestQueue(requireActivity());
                StringRequest retrieveRequestQ = new StringRequest(Request.Method.GET,
                        urlRetrieveImg, response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("userImg")) {
                            // Get the image path ./Uploads
                            String imagePath = jsonObject.getString("userImg");
                            if (isAdded()) {
                                Picasso.get().load(getString(R.string.ip_address) +
                                        "NewsApp/Uploads/" + imagePath).placeholder
                                        (R.drawable.person_icon).error
                                        (R.drawable.error_image).into(imageProfile);
                            }
                        } else if (jsonObject.has("error")) {
                            String error = jsonObject.getString("error");
                            Toast.makeText(getActivity(), "Image fetch failed: " +
                                    error, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Invalid response from server",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "[JSONException]: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(requireActivity(), "[Error]: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show());
                retrieveQueue.add(retrieveRequestQ);
            } catch (UnsupportedEncodingException e) {
                Toast.makeText(getActivity(), "[UnsupportedEncodingException]: " +
                        e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        galleryLauncher = registerForActivityResult
                (new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri filepath = data.getData();
                            try {
                                InputStream inputStream = requireActivity()
                                        .getContentResolver().openInputStream(filepath);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                imageProfile.setImageBitmap(bitmap);
                                encodeBitmapImage(bitmap);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            imgSetBtn.setVisibility(View.GONE); // hide gar di ya jaya set image btn
                        }
                    } else {
                        imgSetBtn.setVisibility(View.GONE); // hide gar di ya jaya set image btn
                    }
                });

        txtAddImage.setOnClickListener(v -> Dexter.withContext(requireActivity())
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted
                            (PermissionGrantedResponse permissionGrantedResponse) {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        galleryLauncher.launch(intent);
                        imgSetBtn.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPermissionDenied
                            (PermissionDeniedResponse permissionDeniedResponse) {
                        // Handle permission denied case
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown
                            (PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check());


        String username = requireActivity().getIntent().getStringExtra("user_name");
        imgSetBtn.setOnClickListener(view -> {
            String ip = getString(R.string.ip_address).trim();
            String urlUploadPath = ip + "NewsApp/upload.php";

            RequestQueue requestQ = Volley.newRequestQueue(rootView.getContext());
            StringRequest strRequest = new StringRequest
                    (Request.Method.POST, urlUploadPath,
                            response -> Toast.makeText(requireActivity(),
                                    "Image set successfully!", Toast.LENGTH_SHORT).show(),
                            error -> Toast.makeText(requireActivity(), "Image set failure!, "
                                    + error.getMessage(), Toast.LENGTH_LONG).show()) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> paramsMap = new HashMap<>();
                    paramsMap.put("username", username);
                    paramsMap.put("upload", encodeImageString);
                    return paramsMap;
                }
            };
            requestQ.add(strRequest);
        });

        // for link making
        Linkify.addLinks(projectFirstTV, Linkify.WEB_URLS);
        Linkify.addLinks(projectSecondTV, Linkify.WEB_URLS);
        Linkify.addLinks(projectSecondHomeTV, Linkify.WEB_URLS);

        profileName.setText(requireActivity().getIntent().getStringExtra("user_name"));
        profileEmail.setText(requireActivity().getIntent().getStringExtra("user_email"));

        Button btnLogout = rootView.findViewById(R.id.logout_button);

        btnLogout.setOnClickListener(v -> {
            // get the session id from sharedPref
            SharedPreferences sharedPreferences =
                    requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
            String session_id = sharedPreferences.getString("session_id", null);
            Toast.makeText(getContext(), "Session_Id: " +
                    session_id, Toast.LENGTH_SHORT).show();
            // Define the URL for logout
            String ip_address = getString(R.string.ip_address).trim();
            String url = ip_address + "NewsApp/logout.php?session_id=" + session_id;
            // Get the RequestQueue
            RequestQueue queue = Volley.newRequestQueue(rootView.getContext());
            // Create a StringRequest for logout
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    response -> {
                        // Parse the JSON response
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean error = jsonObject.getBoolean("error");
                            String message = jsonObject.getString("message");

                            // Check if there is an error
                            if (error) {
                                // Show an error message
                                Toast.makeText(getContext(), "[Error]: " +
                                                message,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                // remove the session id from sharedPref
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("session_id");
                                editor.apply();

                                // Show a success message
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getActivity(), UserLoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);


                                // Finish the current activity (optional)
                                requireActivity().finish();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getContext(), "[JSONException]: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    }, error -> {
                // Handle error
                Toast.makeText(container.getContext(), "[Error]: Network Error, "
                                + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("session_id", session_id);
                    return params;
                }
            };

            // Add the request to the RequestQueue
            queue.add(stringRequest);
        });

        return rootView;
    }

    private void encodeBitmapImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytesOfImage = byteArrayOutputStream.toByteArray();
        encodeImageString = android.util.Base64.encodeToString(bytesOfImage, Base64.DEFAULT);
    }
}
