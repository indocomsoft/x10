/*
 *  This file is part of the X10 project (http://x10-lang.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  (C) Copyright IBM Corporation 2006-2016.
 */

package x10.matrix.comm;

import x10.matrix.ElemType;

/**
 * This class broadcasts data in double-precision or CompressArray to all places
 */
public class ArrayBcast extends ArrayRemoteCopy {
    /**
     * Broadcast data array from here to all other places.
     * Remote places are accessed via PlaceLocalHandle.
     * 
     * @param dmat    distributed storage for source and its copies in all places
     */
    public static def bcast(duplist:DataArrayPLH) {
        val data = duplist();
        bcast(duplist, data.size);
    }

    /**
     * Broadcast data array from here to a place group.
     * Remote places are accessed via PlaceLocalHandle.
     */
    public static def bcast(duplist:DataArrayPLH, pg: PlaceGroup) {
        val data = duplist();
        x10Bcast(duplist, data.size, pg);
    }

    /**
     * Broadcast data array from here to all other places.
     * 
     * @param duplist     distributed storage for source and its copies in all places
     * @param dataCnt     count of double-precision data to broadcast
     */
    public static def bcast(duplist:DataArrayPLH, dataCnt:Long) : void {
        assert (dataCnt <= duplist().size) : "Data overflow in data buffer";
        
        x10Bcast(duplist, dataCnt);
    } 

    /**
     *  Broadcast data to number of places from here
     */
    protected static def x10Bcast(dmlist:DataArrayPLH, dataCnt:Long): void {
        val pcnt = Place.numPlaces();

        if (pcnt <= 1 || dataCnt == 0L) return;
        
        binaryTreeCast(dmlist, dataCnt, pcnt);
    }

    /**
     *  Broadcast data to number of places from here
     */
    protected static def x10Bcast(dmlist:DataArrayPLH, dataCnt:Long, pg: PlaceGroup): void {            
        binaryTreeCast(dmlist, dataCnt, pg, 0, pg.size()-1);
    }

    /**
     * X10 implementation of broadcast data via Binary tree structure.
     */
    protected static def binaryTreeCast(dmlist:DataArrayPLH, dataCnt:Long, pcnt:Long): void {        
        binaryTreeCast(dmlist, dataCnt, Place.places(), 0, pcnt-1);
    }


    /**
     * Broadcast to specified list of places
     */
    protected static def binaryTreeCast(dmlist:DataArrayPLH, dataCnt:Long, pg:PlaceGroup, start:Long, end:Long): void {
        if (end < start) return;
        val src = dmlist();
        assert dataCnt <= src.size;
        val srcbuf = new GlobalRail[ElemType](src as Rail[ElemType]{self!=null});

        val mid = (start+end+1) / 2;        
        finish {
            if (pg(mid) != here) {
                at(pg(mid)) async {
                    val dstbuf = dmlist();
                    assert dataCnt <= dstbuf.size;
                    // remote get
                    finish Rail.asyncCopy[ElemType](srcbuf, 0, dstbuf, 0, dataCnt);
                    // right branch
                    binaryTreeCast(dmlist, dataCnt, pg, mid+1, end);
                }
            }
            // left branch
            binaryTreeCast(dmlist, dataCnt, pg, start, mid-1);
        }
    }

    /**
     * Bcast a segment of data to specified list of places
     */
    public static def bcast(duplist:DataArrayPLH, offset:Long, datCnt:Long, plcList:Rail[Long]) {
        for (var i:Long=0; i<plcList.size; i++) {
            val pid = plcList(i);
            copy(duplist() as Rail[ElemType]{self!=null}, offset, duplist, pid, offset, datCnt);
        }
    }


    // Broadcast SparseCSC matrix to all

    /**
     * Broadcast compress array stored in DistArray
     * at here to all other places. 
     * 
     * @param smlist     compress array data buffer in all places
     */
    public static def bcast(smlist:CompArrayPLH) {
        val data = smlist();
        bcast(smlist, data.storageSize());
    }

    /**
     * Broadcast compress array stored in dist array from here
     * to all other places. 
     * 
     * @param smlist    compress array date buffer in all places
     * @param dataCnt   number of data to broadcast
     */
    public static def bcast(smlist:CompArrayPLH, dataCnt:Long): void {
        assert (dataCnt <= smlist().storageSize()) : "Data overflow in bcast";
        
        x10Bcast(smlist, dataCnt);
    } 

    /**
     *  Broadcast compress array among the pcnt number of places followed from here
     */
    protected static def x10Bcast(smlist:CompArrayPLH, dataCnt:Long): void {
        val pcnt = Place.numPlaces();
        if (pcnt <= 1 || dataCnt == 0L) return;
        
        binaryTreeCast(smlist, dataCnt, pcnt);
    }
    

    /**
     * Broadcast compress array using remote array copy in X10
      * TODO: pcnt not required 
     */
    protected static def binaryTreeCast(smlist:CompArrayPLH, dataCnt:Long, pcnt:Long): void {        
        binaryTreeCast(smlist, dataCnt, Place.places(), 0, pcnt-1);
    }

    protected static def binaryTreeCast(smlist:CompArrayPLH, dataCnt:Long, pg:PlaceGroup, start:Long, end:Long): void {        
        if (end < start) return;            

        // Specify the remote buffer
        val srcca = smlist();
        assert dataCnt <= srcca.index.size : "dataCnt overruns srcca.index";
        assert dataCnt <= srcca.value.size : "dataCnt overruns srcca.value";
        val srcidx = new GlobalRail[Long    ](srcca.index);
        val srcval = new GlobalRail[ElemType](srcca.value);

        val mid = (start+end+1) / 2;
        finish {
            if (pg(mid) != here) {
                at(pg(mid)) async {
                    //Need: smlist, srcidx, srcval, srcOff, colOff, colCnt and datasz
                    val dstca = smlist();
                    assert dataCnt <= dstca.index.size : "dataCnt overruns dstca.index";
                    assert dataCnt <= dstca.value.size : "dataCnt overruns dstca.value";
                    finish {
                        Rail.asyncCopy[Long    ](srcidx, 0, dstca.index, 0, dataCnt);
                        Rail.asyncCopy[ElemType](srcval, 0, dstca.value, 0, dataCnt);
                    }
                    // right branch
                    binaryTreeCast(smlist, dataCnt, pg, mid+1, end);
                }
            }
            // left branch
            binaryTreeCast(smlist, dataCnt, pg, start, mid-1);
        }
    }
    
    public static def verify(srcplh:DataArrayPLH, dataCnt:Long):Boolean {
        var ret:Boolean = true;
        val buf=srcplh();
        for (place in Place.places()) {
            val rmt= at(place) srcplh();//remote capture
            for (var i:Long=0; i<dataCnt; i++) ret &= (buf(i)==rmt(i));
        }
        return ret;
    }
}
