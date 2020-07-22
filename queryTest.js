const elasticSearch = require('elasticsearch');

// var client = new elasticSearch.Client({
//     host: 'localhost:9200',
//     log: 'trace'
// });


// client.msearch({
//     body: [
//         {
//             index: 'shiv'
//         },
//         {
//             query: {
//                 match: {
//                     name: "Sangam"
//                 }
//             }
//         },
//         {
//             index: 'pradeep'
//         },
//         {
//             query: {
//                 match: {
//                     name: "Gau"
//                 }
//             }
//         }
//     ]
// }, function (error, responses) {
//     if (error) {
//         console.log("Search error: " + JSON.stringify(error));
//     }
// });

// curl -XPUT 'localhost:9200/pradeep/Sweet/6?pretty&pretty' -H 'Content-Type: application/json' -d'
// {
//     "name": "Gautam"
// }
// '

// Create elastic search client configurations.
var client = new elasticSearch.Client({
    host: 'http://35.200.229.146/elasticsearch',
    httpAuth: 'user:HUcy9TxxFs95',
    apiVersion: '6.0',
    log: 'trace'
});

client.indices.getMapping({
    index: "unit_products_in_shops",
    type: "products"
});


// To get settings of "unit_products_total" container in elastic search.
// client.indices.getSettings({
//     index: "unit_products_total"
// });

// // Check whether data is null or not. That means if data is null 
// // then it is deleted from database so no need to search.
// // Otherwise simply search the data.
// // Search top 5 products only.
// client.search({
//     index: 'unit_products_total',
//     type: "products",
//     body: {
//         "size": 5,
//         "query": {
//             "bool": {
//                 "must": [
//                     {
//                         "match": { "category": "Sweet" }
//                     },
//                     {
//                         "multi_match": {
//                             "fields": [
//                                 "full_product_name",
//                                 "full_product_name.std"
//                             ],
//                             "query": "res",
//                             "type": "most_fields",
//                             "fuzziness": "AUTO"
//                         }
//                     }
//                 ]
//             }
//         }
//     }
// }, function (error, response) {
//     if (error) {
//         console.log("Shiv Search Global error: " + error);
//     } else {

//         // Make the data writable to firebase database.
//         var sources = [];
//         var total = response.hits.total;
//         var hits_var = response.hits.hits;

//         var i = 0;
//         for (; i < total; i++) {
//             sources.push(hits_var[i]._source);
//         }


//         var new_response = {
//             "total" : total,
//             "sources": sources
//         }
//         console.log("Shiv New Responce: " + JSON.stringify(new_response));
//     }
// });