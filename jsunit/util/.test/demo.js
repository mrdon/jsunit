/**
 * \file
 * This is the file.
 */

function B(x, y) {}
B.prototype.memfun = function (q) {}

function C() {}
C.prototype.memvar = 1;

if( true )
{
	/**
	 * This is CC_test.
	 * @treturn String It returns String.
	 */
	function CC_test() {}
	C.prototype.CC = function () {}
	C.prototype.CC.prototype.test = CC_test;

	var vvv;
}

var regex = /function (\w+)([^\{\}]*\))/;

this["ActionScriptContext"].ActionScriptClass = ActionScript;
ActionScript.prototype.context = this["ActionScriptContext"];

/**
 * This is variable v.
 * \type Array<int>
 */
var v = new Array( 5 );

/**
 * \ctor
 * C'tor of x.
 * \tparam float a this is \a a
 * @tparam double b this is \a b
 * This is the long text of the c'tor.
 */
function x(a,b)
{
	/*! A member var. */
	this.xx = " Hi\n\"";
	if( /^$/ )
	{
		this.xxx = 1;
	}
	this.aa = a;
	this.bb = this.xx;

	function N()
	{
	/*! \file xxx */
	}

	N.prototype = new M();
	N.prototype.v = this;
	N.prototype.w = this.xxx;
}
/**
 * This is class x.
 * We can describe x also with a long text.
 */
x.prototype = new y();
x.prototype.fn = B.prototype.memfun;
/**
 * Member function.
 * \type "char *"
 */
x.prototype.anonymous = function()
{ 
	x.prototype.a = 2; 
	return "Hello"; 
}
/**
 * This is class O.
 * \ctor
 * Nothing special.
 * \tparam String s A string.
 */
x.prototype.O = function (s) {}
x.prototype.O.prototype = new y();
/**
 * This is static member a.
 * \type long
 * Long description of a.
 */
x.prototype.a = 1;
x.prototype.xxx = 0;
x.fulfills( A, B );

/**
 * Hi.
 * Function returns "Hi!"
 * \tparam int x this is \a x
 * \type String
 * This is the long text of the function.
 */
function fn2(x)
{
	var str = '"Hi!"';
	str = str.replace( /\"/g, "&quot" );
	return str;
}

/**
 * \class Array
 * Standard ECMA class.
 * \docgen function Array() {}
 */
 /**
 * Test for emptiness.
 * Expand Array with test for emptiness.
 * @treturn Boolean true or false
 */
function Array_isEmpty( array )
{
	return array.length() == 0;
}
Array.prototype.isEmpty = Array_isEmpty;
Array.prototype.test = C.prototype.CC.prototype.test;
/** 
 * Equivalent for toString.
 * \treturn String Returns array as String.
 */
Array.prototype._toString = Array.prototype.toString;
Array.prototype._isEmpty = Array.prototype.isEmpty;
Array.prototype.member = 1;
Array.prototype.func = C.prototype.CC;
Array.prototype.isTest1 = this.window != null;
Array.prototype.isTest2 = this.window == null;
Array.prototype.isTest =
	   Array.prototype.isTest1
	|| Array.prototype.isTest2;

/**
 * @ctor
 * This is a ctor of a struct
 */
function struct()
{
	/**
	 * A variable.
	 * \type Number
	 */
	this.x = 0;
}

