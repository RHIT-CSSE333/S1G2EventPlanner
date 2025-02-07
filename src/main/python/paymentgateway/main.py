import json
import random
import string

from flask import Flask, render_template, request, abort, redirect, jsonify


app = Flask(__name__)
PAYMENT_CONFIRMATIONS_DB_FILEPATH = 'payment_confirmations.json'


def store_payment_confirmation(confirmation_id):
    # Yes, it's no bueno for race conditions, but we'll assume it is alright
    # because in the real world, we'd use Stripe or PayPal anyway
    
    with open(PAYMENT_CONFIRMATIONS_DB_FILEPATH) as f:
        current_confirmations = json.load(f)
    
    current_confirmations.append(confirmation_id)
    
    with open(PAYMENT_CONFIRMATIONS_DB_FILEPATH, 'w') as f:
        json.dump(current_confirmations, f)


def verify_payment_confirmation(confirmation_id):
    with open(PAYMENT_CONFIRMATIONS_DB_FILEPATH) as f:
        current_confirmations = json.load(f)
    
    return confirmation_id in current_confirmations


def generate_payment_confirmation():
    characters = string.ascii_letters + string.digits + '_.-'
    confirmation = ''.join(random.choice(characters) for _ in range(50))
    store_payment_confirmation(confirmation)
    
    return confirmation


def build_callback_url(url, item_id, payment_confirmation_id):
    return f'{url}?item={item_id}&confirmation={payment_confirmation_id}'


@app.route('/pay/', methods=['GET', 'POST'])
def pay():
    callback_url = request.args.get('callback')
    amount = request.args.get('amount')
    item_id = request.args.get('item')
    
    if not callback_url or not amount or not item_id:
        abort(400)
    
    if request.method == 'GET':
        return render_template('pay.html', amount=amount)
    
    ccno = request.form['ccno']
    
    if ccno == '4242424242424242':
        payment_confirmation_id = generate_payment_confirmation() 
        callback_url = build_callback_url(callback_url, item_id, payment_confirmation_id)    
        return redirect(callback_url)
    
    return render_template('pay.html', amount=amount, error='Payment not successful. Please verify your card information (test card #: 4242 4242 4242 4242)')
    

@app.route('/api/verifypayment/<confirmation_id>')
def verify_payment(confirmation_id):
    return jsonify({"valid": verify_payment_confirmation(confirmation_id)})


if __name__ == '__main__':
    app.run()
