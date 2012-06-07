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
	log_NOTICE("complete called, affected items count [", pantheios::integer(transaction.opcData.GetCount()),"] trs id [",pantheios::integer(transaction.getCancelId()),"]");

	for(POSITION pos = transaction.opcData.GetStartPosition(); pos != NULL; )
	{
		COPCItem_DataMap::CPair* pPair = transaction.opcData.GetNext(pos);
		
		OPCItemData* pItemData = pPair->m_value;
		
		std::string value = Utils::VariantToStringConverter(pItemData->vDataValue);
		log_NOTICE("\t completed item [",pPair->m_key->getName(),"] value [", value,"]");
	}

}
