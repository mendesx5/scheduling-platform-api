-- Uma assinatura recém-criada fica com status PAYMENT_PENDING até o pagamento
-- ser confirmado pelo Asaas (webhook CHECKOUT_PAID). start_date só é preenchida
-- nesse momento, então não pode ser NOT NULL desde a criação do registro.
ALTER TABLE subscriptions ALTER COLUMN start_date DROP NOT NULL;
