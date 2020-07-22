/**
 * Author: Shiv Bhushan Tripathi
 * Copyright: IKAI services private ltd.
 */

const functions = require('firebase-functions');
const elasticSearch =  require('elasticsearch');
const admin = require('firebase-admin');








/**
 * Not tested highly risky.
 */













// Initailize the app.
admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });

// Function to trigger when some product is added in shop.
exports.addToElasticSearchShopProduct = functions.database
    .ref('/UNiTSellerToProduct/{userKey}/{category}/{pushId}')
    .onWrite(event => {

        // Create elastic search client configurations.
        var client = new elasticSearch.Client({
            host: 'http://35.200.229.146/elasticsearch',
            httpAuth: 'user:HUcy9TxxFs95',
            apiVersion: '6.0',
            log: 'trace'
        });

        // Check whether data is null or not. That means if data is null 
        // then it is deleted from database hence we have to delete it from
        // our index also. Otherwise simply insert the data.
        var data = event.data.val();
        if (data == null) {
            // Delete the data from elastic search.
            return client.deleteByQuery({
                index: "unit_products_in_shops",
                type: "products",
                ignore: [404],                
                body: {
                    query: {
                        bool: {
                            must: [
                                { term: { user_key: event.params.userKey } },
                                { term: { category: event.params.category } },
                                { term: { id: event.params.pushId } }
                            ]
                        }
                    }
                }

            }, function (error, response) {
                if (error) {
                    console.log("Shiv In Shop Deletion error: " + error);
                }
            });

        } else {
            // Insert the data in elastic search.
            return client.index({
                index: "unit_products_in_shops",
                type: "products",
                id: event.params.pushId,
                body: {
                    user_key: event.params.userKey,
                    id: event.params.pushId,
                    category: event.params.category,
                    full_product_name: data.full_product_name,
                    price: data.price,
                    deal_price: data.deal_price,
                    pid: data.pid,
                    last_modified: data.last_modified,
                    created_at: data.created_at,
                    number_of_items: data.number_of_items,
                    number_of_unit_sold: data.number_of_unit_sold
                }
            }, function (error, response) {
                if (error) {
                    console.log("Shiv In shop Creation/ Updation error: " + error);
                }
            });
        }
    }
);

// Function to trigger when some product in added in products container.
exports.addToElasticSearchProduct = functions.database
    .ref('/Products/{pushId}')
    .onWrite( event => {
        // Create elastic search client configurations.
        var client = new elasticSearch.Client({
            host: 'http://35.200.229.146/elasticsearch',
            httpAuth: 'user:HUcy9TxxFs95',
            apiVersion: '6.0',
            log: 'trace'
        });

        // Check whether data is null or not. That means if data is null 
        // then it is deleted from database hence we have to delete it from
        // our index also. Otherwise simply insert the data.
        var data = event.data.val();
        if (data == null) {
            // Delete the data from elastic search.
            return client.delete({
                index: "unit_products_total",
                type: "products",
                id: event.params.pushId,
                ignore: [404]
            }, function (error, response) {
                if (error) {
                    console.log("Shiv Globally Deletion error: " + error);
                }
            });

        } else {
            // Insert the data in elastic search.
            return client.index({
                index: "unit_products_total",
                type: "products",
                id: event.params.pushId,
                body: {
                    id: event.params.pushId,
                    category: data.category,
                    name: data.name,
                    company: data.company,
                    type: data.type,
                    net_weight_or_quantity: data.net_weight_or_quantity,
                    veg_or_non_veg: data.veg_or_non_veg,
                    suitable_for: data.suitable_for,
                    colour: data.colour,
                    fabric: data.fabric,
                    material: data.material,
                    size: data.size,
                    ingredient: data.ingredient,
                    description: data.description,
                    images: data.images,
                    thumbnail: data.thumbnail,
                    first_created_at: data.first_created_at,
                    number_of_unit_sold: data.number_of_unit_sold
                }
            }, function (error, response) {
                if (error) {
                    console.log("Shiv Globally Creation/ Updation error: " + error);
                }
            });
        }
    }
);

/**
 * Todo: Need to implement...
 */
// Function to create thumbnail when something is written in storage having
// reference 'ProductImages/Beverage/{pushId}'.
// exports.createThumbnailForProduct = functions.storage.bucket

// Function to trigger when something is added in ProductSearchInShop container.
exports.searchInElasticSearchProductsInShop = functions.database
    .ref("ProductSearchInShop/{userKey}")
    .onWrite( event => {
        // Create elastic search client configurations.
        var client = new elasticSearch.Client({
            host: 'http://35.200.229.146/elasticsearch',
            httpAuth: 'user:HUcy9TxxFs95',
            apiVersion: '6.0',
            log: 'trace'
        });

        // Check whether data is null or not. That means if data is null 
        // then it is deleted from database so no need to search.
        // Otherwise simply search the data.
        var data = event.data.val();
        if (data != null) {
            // Search top 5 products only.
            client.search({
                index: 'unit_products_in_shops',
                type: "products",
                body: {
                    "size": data.size,
                    "query": {
                        "bool": {
                            "must": [
                                {
                                    "term": { "category": data.category }
                                },
                                {
                                    "multi_match": {
                                        "fields": [
                                            "full_product_name",
                                            "full_product_name.std"
                                        ],
                                        "query": data.query,
                                        "type": "most_fields",
                                        "fuzziness": "AUTO"
                                    }
                                }
                            ]
                        }
                    }
                }
            }, function (error, response) {
                if (error) {
                    console.log("Shiv Search in shop error: " + error);
                } else {

                    // Store hits in some other variable.
                    var hits = response.hits.hits;

                    // If we get some result then search global fields of
                    // products otherwise simply insert data into firebase
                    // database.
                    if (response.hits.total == 0) {

                        var result = {
                            "total": 0,
                            "sources_global": null,
                            "sources_local": null
                        }

                        // Write the result back to database and return
                        // the promise.
                        return (admin.database()
                            .ref("ProductSearchResultInShop/"
                                + event.params.userKey + "/" + "1")
                            .set(result, function (error) {
                                if (error) {
                                    console.log("Shiv: Data can not"
                                        + " be saved." + error);
                                } else {
                                    console.log("Shiv: Data is"
                                        + " be saved.");
                                }
                            })
                        );
                        
                    } else {

                        // Make some initial push id to insert records in
                        // firebase database.
                        var push_id = 1;

                        // Initialise a variable iterate over hits variable.
                        var hits_iterator = 0;

                        var i = 0;
                        for (; i < total; i++) {
                            // Get the searched product details form
                            // unit_products_total.
                            client.search ({
                                index: "unit_products_total",
                                type: "products",
                                body: {
                                    "query": {
                                        "bool": {
                                            "must": [
                                                {
                                                    "term": {
                                                        "id": hits[i]._source.pid
                                                    }
                                                },
                                                {
                                                    "term": {
                                                        "category":
                                                            hits[i]._source.category
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                }
                            }, function (error, response) {
                                if (error) {
                                    console.log("Shiv Searchingindex error: "
                                        + error);
                                } else {

                                    // Prepare data to insert into firebase
                                    // database.
                                    var sources_local = hits[hits_iterator]._source;
                                    var sources_global = response.hits.hits[0]._source;

                                    // Make result to insert in firebase.
                                    var result = {
                                        "total": 1,
                                        "sources_global": sources_global,
                                        "sources_local": sources_local
                                    }

                                    // Need to check it....
                                    // sources_local.push(
                                    //     JSON.parse(hits[i]._source)
                                    // );
                                    // sources_global.push(
                                    //     JSON.parse(
                                    //         response.hits.hits[i]._source
                                    //     )
                                    // );

                                    // Store push id in some other variable.
                                    var push_id_for_insertion = push_id;

                                    // Increment pushId and hits_iterator for
                                    // next record.
                                    push_id = push_id + 1;
                                    hits_iterator = hits_iterator + 1;

                                    // Write the result back to database and
                                    // return the promise.
                                    return (admin.database()
                                        .ref("ProductSearchResultInShop/"
                                            + event.params.userKey + "/"
                                            + push_id_for_insertion)
                                        .set(result, function (error) {
                                            if (error) {
                                                console.log("Shiv: Data can not"
                                                    + " be saved." + error);
                                            } else {
                                                console.log("Shiv: Data is"
                                                    + " be saved.");
                                            }
                                        })
                                    );

                                }
                            });

                        }

                        // var new_response = {
                        //     "total": total,
                        //     "sources_global": sources_global,
                        //     "sources_local": sources_local
                        // }

                        // // Write the result back to database and return the promise.
                        // return (admin.database()
                        //     .ref("ProductSearchResultInShop/" + event.params.userKey)
                        //     .set(JSON.stringify(new_response)));

                    }

                }
            });
        }
    }
);

/**
 * Todo: Need to implement...
 */
// // Function to trigger when something is added in ProductSearchGlobal container.
// exports.searchInElasticSearchProductsGloblly = functions.database
//     .ref("ProductSearchGlobal/{userKey}")
//     .onWrite( event => {
//         // Create elastic search client configurations.
//         var client = new elasticSearch.Client({
//             host: 'http://35.200.229.146/elasticsearch',
//             httpAuth: 'user:HUcy9TxxFs95',
//             apiVersion: '6.0',
//             log: 'trace'
//         });

//         // Check whether data is null or not. That means if data is null 
//         // then it is deleted from database so no need to search.
//         // Otherwise simply search the data.
//         var data = event.data.val();
//         if (data != null) {
//             // Search top 5 products only.
//             return client.search({
//                 index: 'unit_products_total',
//                 type: "products",
//                 body: {
//                     "size": data.size,
//                     "query": {
//                         "bool": {
//                             "must": [
//                                 {
//                                     "match": { "category": data.category }
//                                 },
//                                 {
//                                     "multi_match": {
//                                         "fields": [
//                                             "full_product_name",
//                                             "full_product_name.std"
//                                         ],
//                                         "query": data.query,
//                                         "type": "most_fields",
//                                         "fuzziness": "AUTO"
//                                     }
//                                 }
//                             ]
//                         }
//                     }
//                 }
//             }, function (error, response) {
//                 if (error) {
//                     console.log("Shiv Search Global error: " + error);
//                 } else {

//                     // Make the data writable to firebase database.
//                     var sources = [];
//                     var total = response.hits.total;
//                     var hits = response.hits.hits;
//                     var i = 0;
//                     var new_total = 0;
//                     for (; i < total; i++) {
                        
//                         // Check whether that product is already present or not.
//                         var id = hits[i]._id;
//                         return client.search({
//                             index: "unit_products_in_shops",
//                             type: "products",
//                             body: {
//                                 query: {
//                                     bool: {
//                                         must: [
//                                             {
//                                                 term: {
//                                                     "user_key": event.params.userKey
//                                                 }
//                                             },
//                                             {
//                                                 term: {
//                                                     "pid": id
//                                                 }
//                                             }
//                                         ]
//                                     }
//                                 }
//                             }
//                         }, function (error, response) {
//                             if (error) {
//                                 console.log("Shiv Searching same pid error: "
//                                 + error);
//                             } else {
//                                 if (response.hits.total == 0) {
//                                     new_total = new_total + 1;
//                                     sources.push(hits[i]._source);
//                                 }
//                             }
//                         });

//                     }
//                     var new_response = {
//                         "total": new_total,
//                         "sources": sources
//                     }

//                     // Write the result back to database and return the promise.
//                     // return (admin.database()
//                     //     .ref("ProductSearchGlobalResult/" + event.params.userKey)
//                     //     .set((new_response)));
//                     console.log("response : " + JSON.stringify(new_response.sources));
//                 }
//             });
//         }
//     }
// );

/**
 * Todo : Need to impelement....
 */
// // Function to trigger when something is added in GetProductInShop container.
// exports.getElasticSearchProductsInShop = functions.database
//     .ref("GetProductInShop/{userKey}")
//     .onWrite(event => {
//         // Create elastic search client configurations.
//         var client = new elasticSearch.Client({
//             host: 'http://35.200.229.146/elasticsearch',
//             httpAuth: 'user:HUcy9TxxFs95',
//             apiVersion: '6.0',
//             log: 'trace'
//         });

//         // Check whether data is null or not. That means if data is null 
//         // then it is deleted from database so no need to search.
//         // Otherwise simply search the data.
//         var data = event.data.val();
//         if (data != null) {
//             // Search top 5 products only.
//             client.search({
//                 index: 'unit_products_in_shops',
//                 body: {
//                     "from": data.from,
//                     "size": data.size,
//                     "sort": [
//                         {
//                            "number_of_unit_sold": "desc"
//                         }
//                     ],
//                     "query": {
//                         "match_all" : {}
//                     }
//                 }
//             }, function (error, response) {
//                 if (error) {
//                     console.log("Shiv Search Global error: " + error);
//                 } else {

//                     // Make the data writable to firebase database.
//                     var sources_global = [];
//                     var sources_local = [];
//                     var total = response.hits.total;
//                     var hits = response.hits.hits;
//                     var type = hits[0]._source.category;
//                     var i = 0;
//                     for (; i < total; i++) {
//                         // Get the searched product details form
//                         // unit_products_total.
//                         return client.search({
//                             index: "unit_products_total",
//                             type: type,
//                             body: {
//                                 query: {
//                                     term: { id: hits[i]._source.pid }
//                                 }
//                             }
//                         }, function (error, response) {
//                             if (error) {
//                                 console.log("Shiv Searching_index error: " + error);
//                             } else {
//                                 if (response.hits.total > 0) {
//                                     sources_global
//                                     .push(response.hits.hits[0]._source);
//                                 }
//                             }
//                         });

//                         sources_local.push(hits[i]._source);

//                     }
//                     var new_response = {
//                         "total": total,
//                         "sources_global": sources_global,
//                         "sources_local": sources_local
//                     }

//                     // Write the result back to database and return the promise.
//                     return (admin.database()
//                         .ref("GetProductInShopResult/" + event.params.userKey)
//                         .set(JSON.stringify(new_response)));
//                 }
//             });
//         }
//     }
// );

// Function to trigger when customer information is added in "UNiTCustomer".
exports.addToElasticSearchUNiTCustomer = functions.database
    .ref('/UNiTCustomer/{userKey}')
    .onWrite(event => {

        // Create elastic search client configurations.
        var client = new elasticSearch.Client({
            host: 'http://35.200.229.146/elasticsearch',
            httpAuth: 'user:HUcy9TxxFs95',
            apiVersion: '6.0',
            log: 'trace'
        });

        // Check whether data is null or not. That means if data is null 
        // then it is deleted from database hence we have to delete it from
        // our index also. Otherwise simply insert the data.
        var data = event.data.val();
        if (data == null) {
            // Delete the data from elastic search.
            return client.deleteByQuery({
                index: "unit_customer",
                type: "customer",
                ignore: [404],
                body: {
                    query: {
                        must: {
                            term: {
                                user_key: event.params.userKey
                            }
                        }
                    }
                }

            }, function (error, response) {
                if (error) {
                    console.log("Shiv in unit_customer deletion error: " + error);
                }
            });

        } else {
            // Insert the data in elastic search.
            return client.index({
                index: "unit_customer",
                type: "customer",
                id: event.params.userKey,
                body: {
                    user_key: event.params.userKey,
                    name: data.name,
                    dob: data.dob,  // format should be of date type "DD-MM-yyyy"
                    mobile_number: data.mobile_number,
                    gender: data.gender,
                    access: data.access,   // Block the user.
                    pin_code: data.pin_code
                }
            }, function (error, response) {
                if (error) {
                    console.log("Shiv unit_customer creation/ updation error: "
                    + error);
                }
            });
        }
    }
);

// Function to trigger when seller information is added in "UNiTSellers".
exports.addToElasticSearchUNiTSeller = functions.database
    .ref('/UNiTSellers/{userKey}')
    .onWrite(event => {

        // Create elastic search client configurations.
        var client = new elasticSearch.Client({
            host: 'http://35.200.229.146/elasticsearch',
            httpAuth: 'user:HUcy9TxxFs95',
            apiVersion: '6.0',
            log: 'trace'
        });

        // Check whether data is null or not. That means if data is null 
        // then it is deleted from database hence we have to delete it from
        // our index also. Otherwise simply insert the data.
        var data = event.data.val();
        if (data == null) {
            // Delete the data from elastic search.
            return client.deleteByQuery({
                index: "unit_seller",
                type: "seller",
                ignore: [404],
                body: {
                    query: {
                        must: {
                            term: {
                                user_key: event.params.userKey
                            }
                        }
                    }
                }

            }, function (error, response) {
                if (error) {
                    console.log("Shiv in unit_seller deletion error: " + error);
                }
            });

        } else {
            // Insert the data in elastic search.
            return client.index({
                index: "unit_seller",
                type: "seller",
                id: event.params.userKey,
                body: {
                    user_key: event.params.userKey,
                    owner_name: data.owner_name,
                    categories: data.categories,
                    mobile_number: data.sellerMobNumber,
                    shop_address: data.shopAddress,
                    shop_location: {
                        lat: data.lat,
                        lon: data.lon
                    },
                    shop_images: data.shopImages,
                    shop_name: data.shopName,
                    shop_pin_code: data.shopPinCode,
                    shop_landmark: data.shopLandmark,
                    market: data.market,
                    access: data.access    // To block the seller.
                }
            }, function (error, response) {
                if (error) {
                    console.log("Shiv unit_seller creation/ updation error: "
                        + error);
                }
            });
        }
    }
);


