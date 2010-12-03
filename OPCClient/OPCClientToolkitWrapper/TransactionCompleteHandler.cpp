#include "TransactionCompleteHandler.h"
#include "Utils.h"
#include "OPCItem.h"

#include <pantheios/pantheios.hpp>
#include <pantheios/inserters/integer.hpp>

using namespace pantheios;

static const int nMaxBuffSz = 2000;

TransactionCompleteHandler::TransactionCompleteHandler(void)
{
}

TransactionCompleteHandler::~TransactionCompleteHandler(void)
{
}

void TransactionCompleteHandler::complete(CTransaction &transaction)
{
	char idBuff[64];
	memset(idBuff, 64, 0);
	sprintf_s(idBuff, 64, "%d", transaction.getCancelId());

	log_NOTICE("complete called, affected items count [", pantheios::integer(transaction.opcData.GetCount()),"] trs id [", idBuff,"]");

	for(POSITION pos = transaction.opcData.GetStartPosition(); pos != NULL; )
	{
		COPCItem_DataMap::CPair* pPair = transaction.opcData.GetNext(pos);

		char valueBuff[nMaxBuffSz];
		memset(valueBuff, nMaxBuffSz, 0);
		ConvertOPCItemDataValueToCharArray(*pPair->m_value, valueBuff, nMaxBuffSz);

		log_NOTICE("\t completed item [",pPair->m_key->getName(),"] value [", valueBuff,"]");
	}

}
