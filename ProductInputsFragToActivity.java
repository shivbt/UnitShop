package com.ikai.unitshop;

import com.ikai.unitshop.DataModel.ProductFieldsGlobal;
import com.ikai.unitshop.DataModel.ProductFieldsInShop;

/**
 * Created by shiv on 20/12/17.
 */

public interface ProductInputsFragToActivity {

    void communicate(final ProductFieldsGlobal productFieldsGlobal
            , final ProductFieldsInShop productFieldsInShop
    );

}
