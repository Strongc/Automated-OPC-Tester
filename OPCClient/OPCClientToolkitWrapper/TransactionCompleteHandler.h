#pragma once
#include "transaction.h"

class TransactionCompleteHandler : public ITransactionComplete
{
public:
	TransactionCompleteHandler(void);
	virtual ~TransactionCompleteHandler(void);

	virtual void complete(CTransaction &transaction);
};
