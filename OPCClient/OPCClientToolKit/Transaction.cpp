#include ".\transaction.h"

#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;

static int transactionInstances = 0;


CTransaction::CTransaction(ITransactionComplete * completeCB)
:completed(FALSE), cancelID(0xffffffff), completeCallBack(completeCB){
	transactionInstances++;
	log_NOTICE("Created transaction, count [", pantheios::integer(transactionInstances),"]");
}



CTransaction::CTransaction(CAtlArray<COPCItem *>&items, ITransactionComplete * completeCB)
:completed(FALSE), cancelID(0xffffffff), completeCallBack(completeCB){
	for (unsigned i = 0; i < items.GetCount(); i++){
		opcData.SetAt(items[i],NULL);
	}
	transactionInstances++;
	log_NOTICE("Created transaction (multi item), count [", pantheios::integer(transactionInstances),"]");

}

CTransaction::~CTransaction()
{
	transactionInstances--;
	log_NOTICE("deleted transaction, count [", pantheios::integer(transactionInstances),"]");
}


void CTransaction::setItemError(COPCItem *item, HRESULT error){
	CAtlMap<COPCItem *, OPCItemData *>::CPair* pair = opcData.Lookup(item);
	opcData.SetValueAt(pair,new OPCItemData(error));
}



void CTransaction::setItemValue(COPCItem *item, FILETIME time, WORD qual, VARIANT & val, HRESULT err){
	CAtlMap<COPCItem *, OPCItemData *>::CPair* pair = opcData.Lookup(item);
	opcData.SetValueAt(pair,new OPCItemData(time, qual, val, err));
}


const OPCItemData * CTransaction::getItemValue(COPCItem *item) const{
	const CAtlMap<COPCItem *, OPCItemData *>::CPair* pair = opcData.Lookup(item);
	if (!pair) return NULL; // abigious - we do'nt know if the key does not exist or there is no value - TODO throw exception

	return pair->m_value;
}

void CTransaction::setCompleted(){
	completed = TRUE;
	if (completeCallBack){
		completeCallBack->complete(*this);
	}
}
