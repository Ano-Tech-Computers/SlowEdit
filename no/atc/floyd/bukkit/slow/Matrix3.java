package no.atc.floyd.bukkit.slow;

/**
 * Based on code Copyright (c) 2008-2010  Morten Silcowitz published under the GPL license  
 */
//package jinngine.math;

import org.bukkit.util.Vector;

//3x3 matrix for optimized matrix ops


/**
 * A 3x3 matrix implementation
 */
public class Matrix3 {
  public double a11, a12, a13;
  public double a21, a22, a23;
  public double a31, a32, a33;
  
  public Matrix3() {
      a11=0; a12=0; a13=0;
      a21=0; a22=0; a23=0;
      a31=0; a32=0; a33=0;
  }

   /**
    * Assign the zero matrix to this matrix
    * @return <code>this</code>
    */
  public final Matrix3 assignZero() {
    a11 = 0; a12 = 0; a13 = 0;
    a21 = 0; a22 = 0; a23 = 0;
    a31 = 0; a32 = 0; a33 = 0;
          return this;
  }
  
  /**
   * Construct a 3x3 matrix using specified fields
   * @param a11
   * @param a12
   * @param a13
   * @param a21
   * @param a22
   * @param a23
   * @param a31
   * @param a32
   * @param a33
   */
  public Matrix3(double a11, double a12, double a13, double a21, double a22,
    double a23, double a31, double a32, double a33) {  
  this.a11 = a11;
  this.a12 = a12;
  this.a13 = a13;
  this.a21 = a21;
  this.a22 = a22;
  this.a23 = a23;
  this.a31 = a31;
  this.a32 = a32;
  this.a33 = a33;
}

 /**
  * create a 3x3 matrix using a set of basis vectors
  * @param e1
  * @param e2
  * @param e3
  */
  public Matrix3( Vector e1, Vector e2, Vector e3) {
    a11 = e1.getX();
    a21 = e1.getY();
    a31 = e1.getZ();
    
    a12 = e2.getX();
    a22 = e2.getY();
    a32 = e2.getZ();
    
    a13 = e3.getX();
    a23 = e3.getY();
    a33 = e3.getZ();
  }
  
  /**
   * Construct a new 3x3 matrix as a copy of the given matrix B
   * @param B
   * @throws NullPointerException
   */
  public Matrix3( Matrix3 B) {
   assign(B);
  }

   /**
   * assign the value of B to this Matrix3
   * @param B
   */
  public final Matrix3 assign(Matrix3 B) {
    a11 = B.a11; a12 = B.a12; a13 = B.a13;
    a21 = B.a21; a22 = B.a22; a23 = B.a23;
    a31 = B.a31; a32 = B.a32; a33 = B.a33;
          return this;
  }

  /**
   * Assign the scale matrix given by s, to this matrix
   */
  public final Matrix3 assignScale(final double s) {
    a11 = s; a12 = 0; a13 = 0;
    a21 = 0; a22 = s; a23 = 0;
    a31 = 0; a32 = 0; a33 = s;
          return this;
  }

  /**
   * Assign the non-uniform scale matrix given by s1, s2 and s3, to this matrix
   */
  public final Matrix3 assignScale(double sx, double sy, double sz) {
    a11 = sx; a12 = 0.; a13 = 0.;
    a21 = 0.; a22 = sy; a23 = 0.;
    a31 = 0.; a32 = 0.; a33 = sz;
    return this;
  }

  
  /**
   * Assign the identity matrix to this matrix
   */
  public final Matrix3 assignIdentity() {
    a11 = 1; a12 = 0; a13 = 0;
    a21 = 0; a22 = 1; a23 = 0;
    a31 = 0; a32 = 0; a33 = 1;
          return this;
  }

    public Matrix3 assign(
            double a11, double a12, double a13,
            double a21, double a22, double a23,
            double a31, double a32, double a33) {
        this.a11 = a11;  this.a12 = a12;  this.a13 = a13;
  this.a21 = a21;  this.a22 = a22;  this.a23 = a23;
  this.a31 = a31;  this.a32 = a32;  this.a33 = a33;
        return this;
    }
  /**
   * Get the n'th column vector of this matrix
   * @param n
   * @return
   * @throws IllegalArgumentException
   */
  public final Vector column(int n) {
    switch (n) {
    case 0:
      return new Vector(a11,a21,a31);
    case 1:
      return new Vector(a12,a22,a32);
    case 2:
      return new Vector(a13,a23,a33);
              default:
                  throw new IllegalArgumentException();
    }  
  }
  
  /**
   * Get the n'th row vector of this matrix
   * @param n
   * @return
   */
  public Vector row(int n) {
    switch (n) {
    case 0:
      return new Vector(a11,a12,a13);
    case 1:
      return new Vector(a21,a22,a23);
    case 2:
      return new Vector(a31,a32,a33);
              default:
                  throw new IllegalArgumentException();
    }  
  }

  
  /**
   * Get all column vectors of this matrix
   * @param c1
   * @param c2
   * @param c3
   */
  public void getColumnVectors( Vector c1, Vector c2, Vector c3) {
    c1.setX(a11);
    c1.setY(a21);
    c1.setZ(a31);

    c2.setX(a12);
    c2.setY(a22);
    c2.setZ(a32);
    
    c3.setX(a13);
    c3.setY(a23);
    c3.setZ(a33);
  }
  
  /**
   * Get all row vectors of this matrix
   * @param r1
   * @param r2
   * @param r3
   */
  public void getRowVectors( Vector r1, Vector r2, Vector r3) {
    r1.setX(a11);
    r1.setY(a12);
    r1.setZ(a13);

    r2.setX(a21);
    r2.setY(a22);
    r2.setZ(a23);
    
    r3.setX(a31);
    r3.setY(a32);
    r3.setZ(a33);
  }
    
  /**
   * Return a new identity Matrix3 instance
   * @return
   */
  public static Matrix3 identity() {
    return new Matrix3().assignIdentity();
  }
  
  /**
   * Multiply this matrix by a scalar, return the resulting matrix
   * @param s
   * @return
   */
  public final Matrix3 multiply( double s) {
    Matrix3 A = new Matrix3();
    A.a11 = a11*s; A.a12 = a12*s; A.a13 = a13*s;
    A.a21 = a21*s; A.a22 = a22*s; A.a23 = a23*s;
    A.a31 = a31*s; A.a32 = a32*s; A.a33 = a33*s;    
    return A;
  }
  
  /**
   * Right-multiply by a scaling matrix given by s, so M.scale(s) = M S(s)
   * @param s
   * @return
   */
  public final Matrix3 scale( Vector s ) {
    Matrix3 A = new Matrix3();
    A.a11 = a11*s.getX(); A.a12 = a12*s.getY(); A.a13 = a13*s.getZ();
    A.a21 = a21*s.getX(); A.a22 = a22*s.getY(); A.a23 = a23*s.getZ();
    A.a31 = a31*s.getX(); A.a32 = a32*s.getY(); A.a33 = a33*s.getZ();    
    return A;
  }
  
  /**
   * Multiply this matrix by the matrix A and return the result
   * @param A
   * @return
   */
  public Matrix3 multiply(Matrix3 A) {
    return multiply(this,A,new Matrix3());
  }

    /**
   * Multiply this matrix by the matrix A and return the result
   * @param A
   * @return
   */
  public Matrix3 assignMultiply(Matrix3 A) {
    return multiply(this,A,this);
  }
  
  //C = AxB 
  public static Matrix3 multiply( final Matrix3 A, final Matrix3 B, final Matrix3 C ) {
    //               B | b11 b12 b13
    //                 | b21 b22 b23
    //                 | b31 b32 b33
    //     -------------------------
    //  A  a11 a12 a13 | c11 c12 c13
    //     a21 a22 a23 | c21 c22 c23
    //     a31 a32 a33 | c31 c32 c33  C
    
    double t11 = A.a11*B.a11 + A.a12*B.a21 + A.a13*B.a31;
    double t12 = A.a11*B.a12 + A.a12*B.a22 + A.a13*B.a32;
    double t13 = A.a11*B.a13 + A.a12*B.a23 + A.a13*B.a33;
    
    double t21 = A.a21*B.a11 + A.a22*B.a21 + A.a23*B.a31;
    double t22 = A.a21*B.a12 + A.a22*B.a22 + A.a23*B.a32;
    double t23 = A.a21*B.a13 + A.a22*B.a23 + A.a23*B.a33;
    
    double t31 = A.a31*B.a11 + A.a32*B.a21 + A.a33*B.a31;
    double t32 = A.a31*B.a12 + A.a32*B.a22 + A.a33*B.a32;
    double t33 = A.a31*B.a13 + A.a32*B.a23 + A.a33*B.a33;

    //copy to C
    C.a11 = t11;
    C.a12 = t12;
    C.a13 = t13;

    C.a21 = t21;
    C.a22 = t22;
    C.a23 = t23;

    C.a31 = t31;
    C.a32 = t32;
    C.a33 = t33;

    return C;
  }
  
  //functional
  /**
   * Multiply a vector by this matrix, return the resulting vector
   */
  public final Vector multiply( final Vector v) {
    Vector r = new Vector();
    Matrix3.multiply(this, v, r);
    return r;
  }
  
  
  //A = A^T 
  public Matrix3 assignTranspose() {
    double t;
  t=a12; a12=a21; a21=t;
  t=a13; a13=a31; a31=t;
  t=a23; a23=a32; a32=t;
    return this;
  }
  
  /**
   * Functional method. Transpose this matrix and return the result
   * @return
   */
  public final Matrix3 transpose() {
   return new Matrix3(this).assignTranspose();
  }


  //C = A-B
  public static Matrix3 subtract( final Matrix3 A, final Matrix3 B, final Matrix3 C ) {
    C.a11 = A.a11-B.a11; C.a12 = A.a12-B.a12; C.a13 = A.a13-B.a13;
    C.a21 = A.a21-B.a21; C.a22 = A.a22-B.a22; C.a23 = A.a23-B.a23;
    C.a31 = A.a31-B.a31; C.a32 = A.a32-B.a32; C.a33 = A.a33-B.a33;
    return C;
  }
 /**
   * Substract to this matrix the matrix B, return result in a new matrix instance
   * @param B
   * @return
   */
  public Matrix3 subtract( Matrix3 B ) {
    return subtract(this,B,new Matrix3());
  }
  /**
   * Substract to this matrix the matrix B, return result in a new matrix instance
   * @param B
   * @return
   */
  public Matrix3 assignSubtract( Matrix3 B ) {
    return subtract(this,B,this);
  }
  /**
   * Add to this matrix the matrix B, return result in a new matrix instance
   * @param B
   * @return
   */
  public Matrix3 add( Matrix3 B ) {
    return add(this,B,new Matrix3());
  }
  /**
   * Add to this matrix the matrix B, return result in a new matrix instance
   * @param B
   * @return
   */
  public Matrix3 assignAdd( Matrix3 B ) {
    return add(this,B,this);
  }
  
  //C = A+B
  public static Matrix3 add( final Matrix3 A, final Matrix3 B, final Matrix3 C ) {
    C.a11 = A.a11+B.a11; C.a12 = A.a12+B.a12; C.a13 = A.a13+B.a13;
    C.a21 = A.a21+B.a21; C.a22 = A.a22+B.a22; C.a23 = A.a23+B.a23;
    C.a31 = A.a31+B.a31; C.a32 = A.a32+B.a32; C.a33 = A.a33+B.a33;
    return C;
  }
  
  // rT = (vT)A   NOTE that the result of this is actually a transposed vector!! 
  public static Vector transposeVectorAndMultiply( final Vector v, final Matrix3 A, final Vector r ){
    //            A  | a11 a12 a13
    //               | a21 a22 a23
    //               | a31 a32 a33
    //      ----------------------
    // vT   v1 v2 v3 |  c1  c2  c3
    
    double t1 = v.getX()*A.a11+v.getY()*A.a21+v.getZ()*A.a31;
    double t2 = v.getX()*A.a12+v.getY()*A.a22+v.getZ()*A.a32;
    double t3 = v.getX()*A.a13+v.getY()*A.a23+v.getZ()*A.a33;
    
    r.setX(t1);
    r.setY(t2);
    r.setZ(t3);

    return r;
  }

  /**
   * Multiply v by A, and place result in r, so r = Av
   * @param A 3 by 3 matrix
   * @param v Vector to be multiplied
   * @param r Vector to hold result of multiplication
   * @return Reference to the given Vector r instance
   */
  public static Vector multiply( final Matrix3 A, final Vector v, final Vector r ) {
    //                   
    //               V | v1
    //                 | v2
    //                 | v3                     
    //     -----------------
    //  A  a11 a12 a13 | c1
    //     a21 a22 a23 | c2
    //     a31 a32 a33 | c3   
    
    double t1 = v.getX()*A.a11+v.getY()*A.a12+v.getZ()*A.a13;
    double t2 = v.getX()*A.a21+v.getY()*A.a22+v.getZ()*A.a23;
    double t3 = v.getX()*A.a31+v.getY()*A.a32+v.getZ()*A.a33;
    
    r.setX(t1);
    r.setY(t2);
    r.setZ(t3);
    
    return r;
  }  

  /**
   * Compute the determinant of Matrix3 A
   * @param A
   * @return 
   */
  public double determinant() {
    return a11*a22*a33- a11*a23*a32 + a21*a32*a13 - a21*a12*a33 + a31*a12*a23-a31*a22*a13;
  }
  
/**
 * Compute the inverse of the matrix A, place the result in C
 */
  public static Matrix3 inverse( final Matrix3 A, final Matrix3 C ) {
    double d = (A.a31*A.a12*A.a23-A.a31*A.a13*A.a22-A.a21*A.a12*A.a33+A.a21*A.a13*A.a32+A.a11*A.a22*A.a33-A.a11*A.a23*A.a32);
    double t11 =  (A.a22*A.a33-A.a23*A.a32)/d;
    double t12 = -(A.a12*A.a33-A.a13*A.a32)/d;
    double t13 =  (A.a12*A.a23-A.a13*A.a22)/d;
    double t21 = -(-A.a31*A.a23+A.a21*A.a33)/d;
    double t22 =  (-A.a31*A.a13+A.a11*A.a33)/d;
    double t23 = -(-A.a21*A.a13+A.a11*A.a23)/d;
    double t31 =  (-A.a31*A.a22+A.a21*A.a32)/d;
    double t32 = -(-A.a31*A.a12+A.a11*A.a32)/d;
    double t33 =  (-A.a21*A.a12+A.a11*A.a22)/d;

    C.a11 = t11; C.a12 = t12; C.a13 = t13;
    C.a21 = t21; C.a22 = t22; C.a23 = t23;
    C.a31 = t31; C.a32 = t32; C.a33 = t33;
    return C;
  }

  public final Matrix3 assignInverse() {
      return inverse(this,this);
  }
  public final Matrix3 inverse() {
        return inverse(this,new Matrix3());
  }

  public static Matrix3 scaleMatrix( double xs, double ys, double zs) {
      return new Matrix3().assignScale(xs,ys,zs);
  }
  
  public static Matrix3 scaleMatrix( double s ) {
      return new Matrix3().assignScale(s);      
  }
     
  @Override
  public String toString() {
    return "["+a11+", " + a12 + ", " + a13 + "]\n"
         + "["+a21+", " + a22 + ", " + a23 + "]\n"
         + "["+a31+", " + a32 + ", " + a33 + "]" ;
  }
  
  /**
   * Check matrix for NaN values 
   */
  public final boolean isNaN() {
    return Double.isNaN(a11)||Double.isNaN(a12)||Double.isNaN(a13)
    || Double.isNaN(a21)||Double.isNaN(a22)||Double.isNaN(a23)
    || Double.isNaN(a31)||Double.isNaN(a32)||Double.isNaN(a33);
  }
  
  public double[] toArray() {
       return new double[]{
                  a11, a21, a31,
                  a12, a22, a32,
                  a13, a23, a33};
  }

  /**
   * Return the Frobenius norm of this Matrix3
   * @return
   */
  public final double fnorm() {
    return Math.sqrt(  a11*a11 + a12*a12 + a13*a13  + a21*a21 + a22*a22  + a23*a23  + a31*a31 + a32*a32 + a33*a33 ); 
  }
    /**
     *
     * @param v
     * @return
     * @throws NullPointerException
     */
    public static Matrix3 crossProductMatrix(Vector v) {
        return new Matrix3(
                0., -v.getZ(), v.getY(),
                v.getZ(), 0., -v.getX(),
                -v.getY(), v.getX(), 0.);
    }
}
