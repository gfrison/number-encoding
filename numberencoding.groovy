/**
 * @author Giancarlo Frison - giancarlo@gfrison.com - http://gfrison.com
 */

package com.gfrison

def map = [['e'],['j','n','q'],['r','w','x'],['d','s','y'],['f','t'],['a','m'],['c','i','v'], ['b','k','u'],['l','o','p'],['g','h','z']]

def dictionary=[]
def testDictionary ="""
an
blau
Bo"
Boot
bo"s
da
Fee
fern
Fest
fort
je
jemand
mir
Mix
Mixer
Name
neu
o"d
Ort
so
Tor
Torf
Wasser
""".split().toList()

def testdata=['112':[],
	'5624-82':['5624-82: mir Tor','5624-82: Mix Tor'],
	'4824':['4824: Torf','4824: fort','4824: Tor 4'],
	'10/783--5':['10/783--5: neu o"d 5','10/783--5: je bo"s 5','10/783--5: je Bo" da'],
	'381482':['381482: so 1 Tor'],
	'04824':['04824: 0 Torf','04824: 0 fort','04824: 0 Tor 4']]

/**
 * select all possible words from specific position
 */
def numFromPos={pnum, pos, possibleWords->
	def completedWords=[]
	int posWord=0
	def tmpWords = possibleWords.clone()
	for(int posNum=pos;posNum<pnum.chars.length;posNum++){
		char  pchar = pnum.chars[posNum]
		def newlist = []
		if(pchar.isDigit()){
			def mapos = Character.getNumericValue(pchar)
			def lettersnum = map.get (mapos)
			for(String word:tmpWords){
				def purgeWord= word.replace('\"','')
				if(purgeWord.size()<=posWord)
					continue
				def letter = Character.toLowerCase(purgeWord.charAt(posWord)).toString()
				if(lettersnum.contains(letter)){
					newlist<<word
					if(posWord==purgeWord.length()-1)
						completedWords<< new Encoding(word:word,posNumber:posNum+1)
				}
			}
			tmpWords=newlist
			posWord++
		}
	}
	return completedWords
}

/**
 * create encoding tree data for each number
 */
def intoEncoding={phonenumber,enc->
	assert phonenumber,enc!=null
	if(phonenumber.length()==enc.posNumber)
		return true
	def encodings = numFromPos(phonenumber, enc.posNumber,dictionary.clone())
	if(encodings.isEmpty()){
		if(!enc.word?.isNumber()){
			if(phonenumber.chars[enc.posNumber].isDigit()){
				def numeric=new Encoding(parent:enc, word:phonenumber.chars[enc.posNumber],posNumber:enc.posNumber+1)
				enc.children<< numeric
				return call(phonenumber,numeric)
			}else{
				enc.posNumber++
				return call(phonenumber,enc)
			}
		}else return false
	}
	boolean oneEncoding=false
	for(it in encodings){
		if(call(phonenumber, it)){
			enc.children<<it
			it.parent=enc
			oneEncoding=true
		}
	}
	return oneEncoding
}
def parseNumber={pnum ->
	def encoding =new Encoding(posNumber:0)
	if(intoEncoding(pnum,encoding))
		return encoding.printEncodings(pnum,[])
	else []
		
}

/**
 * represents encoding data structure. tree of all acceptable words for a specific number
 * @author gfrison
 *
 */
class Encoding{
	String word
	int posNumber = 0
	def children = []  //Encoding type
	Encoding parent
	
	def printEncodings={phone,list->
		if(children.isEmpty()){
			def encoding=printWord()?.trim()
			if(encoding)
				list<< phone+': '+encoding
		}else{
			def ret = []
			children.each{
				ret<<it.printEncodings(phone,list)
			}
		}
		list
	}
	
	String printWord(){
		(word?(parent?.printWord() +word+' '):'') 
	}
	

}

def params = args as List
if(params.contains('--test')){
	dictionary=testDictionary
	testdata.each{phone,expected->
		println 'test phone:'+phone
		results = parseNumber(phone)
		assert expected.size()==results.size()
		results.each{
			println it
			assert expected.contains(it)
		}
	}
	println ''
	println 'Test OK'
	return
}
if(params.size()!=2){
	println 'usage:'
	println 'groovy numberencoding.groovy file-dictionary file-numbers'
	return
}
//load dictionary
dictionary = new File(params[0]).text.split() as List
//load phone numbers as stream
new File(params[1]).eachLine{
	parseNumber(it.trim()).each{
		println it
	}
}